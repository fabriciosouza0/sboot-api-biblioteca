package com.escola.biblioteca.domain.event;

import java.util.Map;
import java.util.UUID;

public interface DomainEventPublisher {

    void publish(String aggregateType, UUID aggregateId, String eventType, Object payload, Map<String, String> metadata);
}