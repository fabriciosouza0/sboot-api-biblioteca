package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.OutboxEvent;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends CrudRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByProcessedAtIsNullOrderByCreatedAtAsc(int limit);
}