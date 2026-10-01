package com.escola.biblioteca.domain.outbox.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent implements Persistable<UUID> {

    @Id
    private UUID id;

    private String aggregateType;

    private UUID aggregateId;

    private String eventType;

    private String payload;

    private String metadata = "{}";

    private OffsetDateTime createdAt;

    private OffsetDateTime processedAt;

    @Transient
    private boolean novo;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return novo;
    }

    public void marcarNovo() {
        this.novo = true;
    }
}