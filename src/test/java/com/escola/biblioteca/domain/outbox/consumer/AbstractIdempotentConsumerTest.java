package com.escola.biblioteca.domain.outbox.consumer;

import com.escola.biblioteca.domain.outbox.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractIdempotentConsumerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private TestConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TestConsumer(processedEventRepository);
    }

    @Test
    void processMessage_quandoEventoJaFoiProcessado_ignoraExecucao() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessageWithEventId(eventId, "{\"key\":\"val\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, "test-consumer")).thenReturn(true);

        AtomicBoolean executed = new AtomicBoolean(false);
        consumer.execute(message, payload -> executed.set(true));

        assertThat(executed.get()).isFalse();
        verify(processedEventRepository, never()).markAsProcessed(any(), any());
    }

    @Test
    void processMessage_quandoEventoENovo_executaEMarcaComoProcessado() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessageWithEventId(eventId, "{\"key\":\"val\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, "test-consumer")).thenReturn(false);

        AtomicBoolean executed = new AtomicBoolean(false);
        consumer.execute(message, payload -> executed.set(true));

        assertThat(executed.get()).isTrue();
        verify(processedEventRepository).markAsProcessed(eq(eventId), eq("test-consumer"));
    }

    @Test
    void processMessage_quandoHandlerFalha_naoMarcaComoProcessado() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessageWithEventId(eventId, "{\"key\":\"val\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, "test-consumer")).thenReturn(false);

        assertThatThrownBy(() -> consumer.execute(message, payload -> {
            throw new IllegalStateException("Simulated processing error");
        })).isInstanceOf(IllegalStateException.class);

        verify(processedEventRepository, never()).markAsProcessed(any(), any());
    }

    private Message createMessageWithEventId(UUID eventId, String value) {
        MessageProperties properties = new MessageProperties();
        properties.setHeader("event-id", eventId.toString());
        return new Message(value.getBytes(StandardCharsets.UTF_8), properties);
    }

    private static class TestConsumer extends AbstractIdempotentConsumer {
        public TestConsumer(ProcessedEventRepository repository) {
            super(repository, "test-consumer");
        }

        public void execute(Message message, EventRecordHandler handler) {
            super.processMessage(message, handler);
        }
    }
}
