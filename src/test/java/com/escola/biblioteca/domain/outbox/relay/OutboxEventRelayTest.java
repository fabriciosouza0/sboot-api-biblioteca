package com.escola.biblioteca.domain.outbox.relay;

import com.escola.biblioteca.config.RabbitConfig;
import com.escola.biblioteca.domain.outbox.model.OutboxEvent;
import com.escola.biblioteca.domain.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private OutboxEventRelay relay;

    @BeforeEach
    void setUp() {
        relay = new OutboxEventRelay(outboxEventRepository, rabbitTemplate, Collections.emptyList());
    }

    @Test
    void processOutbox_quandoNaoHaEventos_naoFazNada() {
        when(outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(100))
                .thenReturn(Collections.emptyList());

        relay.processOutbox();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void processOutbox_comEventoLoan_publicaNoRabbitMQComRoutingKeyEHeaders() {
        UUID eventId = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent();
        event.setId(eventId);
        event.setAggregateType("Loan");
        event.setAggregateId(loanId);
        event.setEventType("LoanCreated");
        event.setPayload("{\"id\":\"" + loanId + "\"}");
        event.setCreatedAt(OffsetDateTime.now());

        when(outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(100))
                .thenReturn(List.of(event));

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        relay.processOutbox();

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE_EVENTS),
                eq(RabbitConfig.ROUTING_KEY_CIRCULATION_LOAN),
                eq(event.getPayload()),
                postProcessorCaptor.capture()
        );

        Message originalMessage = new Message(new byte[0], new MessageProperties());
        Message processedMessage = postProcessorCaptor.getValue().postProcessMessage(originalMessage);

        assertThat((String) processedMessage.getMessageProperties().getHeader("event-id")).isEqualTo(eventId.toString());
        assertThat((String) processedMessage.getMessageProperties().getHeader("event-type")).isEqualTo("LoanCreated");
        assertThat((String) processedMessage.getMessageProperties().getHeader("aggregate-type")).isEqualTo("Loan");
        assertThat((String) processedMessage.getMessageProperties().getHeader("aggregate-id")).isEqualTo(loanId.toString());

        verify(outboxEventRepository).save(event);
        assertThat(event.getProcessedAt()).isNotNull();
    }

    @Test
    void processOutbox_quandoRabbitMQLancaExcecao_naoMarcaComoProcessado() {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setAggregateType("Loan");
        event.setAggregateId(UUID.randomUUID());
        event.setEventType("LoanCreated");
        event.setPayload("{}");

        when(outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(100))
                .thenReturn(List.of(event));

        doThrow(new AmqpException("Broker connection timeout"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(), any(MessagePostProcessor.class));

        relay.processOutbox();

        verify(outboxEventRepository, never()).save(event);
        assertThat(event.getProcessedAt()).isNull();
    }
}
