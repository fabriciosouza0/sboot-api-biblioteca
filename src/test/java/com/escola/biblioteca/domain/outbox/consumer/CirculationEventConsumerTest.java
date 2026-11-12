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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CirculationEventConsumerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private CirculationEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new CirculationEventConsumer(processedEventRepository);
    }

    @Test
    void onLoanEvent_quandoEventoNovo_marcaComoProcessado() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessage(eventId, "{\"loanId\":\"" + UUID.randomUUID() + "\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, CirculationEventConsumer.CONSUMER_ID)).thenReturn(false);

        consumer.onLoanEvent(message);

        verify(processedEventRepository).markAsProcessed(eq(eventId), eq(CirculationEventConsumer.CONSUMER_ID));
    }

    @Test
    void onHoldEvent_quandoEventoJaProcessado_ignora() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessage(eventId, "{\"holdId\":\"" + UUID.randomUUID() + "\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, CirculationEventConsumer.CONSUMER_ID)).thenReturn(true);

        consumer.onHoldEvent(message);

        verify(processedEventRepository, never()).markAsProcessed(any(), any());
    }

    @Test
    void onFineEvent_quandoEventoNovo_marcaComoProcessado() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessage(eventId, "{\"fineId\":\"" + UUID.randomUUID() + "\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, CirculationEventConsumer.CONSUMER_ID)).thenReturn(false);

        consumer.onFineEvent(message);

        verify(processedEventRepository).markAsProcessed(eq(eventId), eq(CirculationEventConsumer.CONSUMER_ID));
    }

    @Test
    void onCatalogItemEvent_quandoEventoNovo_marcaComoProcessado() {
        UUID eventId = UUID.randomUUID();
        Message message = createMessage(eventId, "{\"itemId\":\"" + UUID.randomUUID() + "\"}");

        when(processedEventRepository.isAlreadyProcessed(eventId, CirculationEventConsumer.CONSUMER_ID)).thenReturn(false);

        consumer.onCatalogItemEvent(message);

        verify(processedEventRepository).markAsProcessed(eq(eventId), eq(CirculationEventConsumer.CONSUMER_ID));
    }

    private Message createMessage(UUID eventId, String payload) {
        MessageProperties properties = new MessageProperties();
        properties.setHeader("event-id", eventId.toString());
        return new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
    }
}
