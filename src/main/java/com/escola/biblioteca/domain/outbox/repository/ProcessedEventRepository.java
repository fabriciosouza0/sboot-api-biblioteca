package com.escola.biblioteca.domain.outbox.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProcessedEventRepository {

    private final JdbcClient jdbcClient;

    public boolean isAlreadyProcessed(UUID eventId, String consumerId) {
        String sql = """
            SELECT COUNT(*)
            FROM processed_event
            WHERE event_id = :eventId AND consumer_id = :consumerId
            """;
        Integer count = jdbcClient.sql(sql)
                .param("eventId", eventId)
                .param("consumerId", consumerId)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    public void markAsProcessed(UUID eventId, String consumerId) {
        String sql = """
            INSERT INTO processed_event (event_id, consumer_id, processed_at)
            VALUES (:eventId, :consumerId, now())
            ON CONFLICT (event_id, consumer_id) DO NOTHING
            """;
        jdbcClient.sql(sql)
                .param("eventId", eventId)
                .param("consumerId", consumerId)
                .update();
    }
}
