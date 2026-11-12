package com.escola.biblioteca.domain.outbox.consumer;

import com.escola.biblioteca.domain.outbox.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractIdempotentConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final String consumerId;

    protected void processMessage(Message message, EventRecordHandler handler) {
        UUID eventId = extractEventId(message);
        if (eventId == null) {
            log.warn("Mensagem recebida sem cabeçalho 'event-id'. Processando sem deduplicação.");
            handler.handle(new String(message.getBody(), StandardCharsets.UTF_8));
            return;
        }

        if (processedEventRepository.isAlreadyProcessed(eventId, consumerId)) {
            log.info("Evento já processado ignorado (idempotência): eventId={}, consumerId={}", eventId, consumerId);
            return;
        }

        handler.handle(new String(message.getBody(), StandardCharsets.UTF_8));
        processedEventRepository.markAsProcessed(eventId, consumerId);
    }

    private UUID extractEventId(Message message) {
        Object headerVal = message.getMessageProperties().getHeader("event-id");
        if (headerVal != null) {
            try {
                return UUID.fromString(headerVal.toString());
            } catch (IllegalArgumentException e) {
                log.warn("Formato de UUID inválido no cabeçalho 'event-id': {}", headerVal);
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface EventRecordHandler {
        void handle(String payload);
    }
}
