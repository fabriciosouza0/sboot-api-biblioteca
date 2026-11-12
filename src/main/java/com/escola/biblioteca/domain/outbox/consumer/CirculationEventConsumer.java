package com.escola.biblioteca.domain.outbox.consumer;

import com.escola.biblioteca.config.RabbitConfig;
import com.escola.biblioteca.domain.outbox.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.worker.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CirculationEventConsumer extends AbstractIdempotentConsumer {

    public static final String CONSUMER_ID = "circulation-worker";

    public CirculationEventConsumer(ProcessedEventRepository repository) {
        super(repository, CONSUMER_ID);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CIRCULATION_LOAN)
    public void onLoanEvent(Message message) {
        processMessage(message, payload -> {
            log.info("[Worker] Evento de empréstimo recebido para processamento assíncrono: {}", payload);
        });
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CIRCULATION_HOLD)
    public void onHoldEvent(Message message) {
        processMessage(message, payload -> {
            log.info("[Worker] Evento de reserva recebido para processamento assíncrono: {}", payload);
        });
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CIRCULATION_FINE)
    public void onFineEvent(Message message) {
        processMessage(message, payload -> {
            log.info("[Worker] Evento de multa recebido para processamento assíncrono: {}", payload);
        });
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CATALOG_ITEM)
    public void onCatalogItemEvent(Message message) {
        processMessage(message, payload -> {
            log.info("[Worker] Evento de catálogo/exemplar recebido para processamento assíncrono: {}", payload);
        });
    }
}
