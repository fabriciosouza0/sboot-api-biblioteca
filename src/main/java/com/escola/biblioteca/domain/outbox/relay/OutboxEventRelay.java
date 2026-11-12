package com.escola.biblioteca.domain.outbox.relay;

import com.escola.biblioteca.config.RabbitConfig;
import com.escola.biblioteca.domain.outbox.model.OutboxEvent;
import com.escola.biblioteca.domain.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.worker.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final List<DomainEventPublisher> publishers;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(100);
        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            try {
                String routingKey = resolveRoutingKey(event.getAggregateType());
                String partitionKey = event.getAggregateId() != null ? event.getAggregateId().toString() : "";

                // Publica no RabbitMQ (TopicExchange)
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_EVENTS, routingKey, event.getPayload(), message -> {
                    MessageProperties props = message.getMessageProperties();
                    props.setHeader("event-id", event.getId().toString());
                    props.setHeader("event-type", event.getEventType());
                    props.setHeader("aggregate-type", event.getAggregateType());
                    props.setHeader("aggregate-id", partitionKey);
                    if (event.getCreatedAt() != null) {
                        props.setHeader("created-at", event.getCreatedAt().toString());
                    }
                    props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    return message;
                });

                // Notifica eventuais publishers internos registrados
                for (DomainEventPublisher publisher : publishers) {
                    publisher.publish(event.getAggregateType(), event.getAggregateId(),
                            event.getEventType(), event.getPayload(), Map.of());
                }

                event.setProcessedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
                log.info("Evento outbox publicado no RabbitMQ: routingKey={}, type={}, id={}",
                        routingKey, event.getEventType(), event.getId());
            } catch (Exception e) {
                log.error("Erro ao publicar outbox event id={} no RabbitMQ: {}", event.getId(), e.getMessage(), e);
                // Não marca como processado; será retentado na próxima execução
            }
        }
    }

    private String resolveRoutingKey(String aggregateType) {
        if (aggregateType == null) {
            return RabbitConfig.ROUTING_KEY_CIRCULATION_LOAN;
        }
        return switch (aggregateType.toUpperCase()) {
            case "LOAN" -> RabbitConfig.ROUTING_KEY_CIRCULATION_LOAN;
            case "HOLD" -> RabbitConfig.ROUTING_KEY_CIRCULATION_HOLD;
            case "FINE" -> RabbitConfig.ROUTING_KEY_CIRCULATION_FINE;
            case "ITEM", "WORK" -> RabbitConfig.ROUTING_KEY_CATALOG_ITEM;
            default -> "circulation." + aggregateType.toLowerCase();
        };
    }
}
