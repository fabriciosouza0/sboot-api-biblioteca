package com.escola.biblioteca.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("hold")
@Getter
@Setter
@NoArgsConstructor
public class Hold implements Persistable<UUID> {

    @Id
    private UUID id;

    private UUID patronId;

    private UUID workId;

    private UUID libraryId;

    private HoldStatus status = HoldStatus.WAITING;

    private Integer position;

    private Instant placedAt;

    private Instant readyAt;

    private Instant expiresAt;

    private Instant fulfilledAt;

    private Instant cancelledAt;

    @Version
    private Long version = 0L;

    private Instant createdAt;

    private Instant updatedAt;

    private boolean readyExpired;

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