package com.escola.biblioteca.domain.outbox.relay;

import com.escola.biblioteca.domain.outbox.model.OutboxEvent;
import com.escola.biblioteca.domain.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final List<DomainEventPublisher> publishers;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedAtIsNullOrderByCreatedAtAsc(100);
        if (events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            try {
                // TODO (BE-018): Publish to NATS JetStream
                // For now, just log and mark as processed
                log.info("Processing outbox event: type={}, aggregateId={}, aggregateType={}",
                        event.getEventType(), event.getAggregateId(), event.getAggregateType());

                // Call registered publishers (for internal consumers)
                for (DomainEventPublisher publisher : publishers) {
                    publisher.publish(event.getAggregateType(), event.getAggregateId(),
                            event.getEventType(), event.getPayload(), Map.of());
                }

                event.setProcessedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Erro ao processar outbox event id={}: {}", event.getId(), e.getMessage(), e);
                // Don't mark as processed; will retry on next run
            }
        }
    }
}