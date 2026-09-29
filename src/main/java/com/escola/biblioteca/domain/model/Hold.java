package com.escola.biblioteca.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
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

    private OffsetDateTime placedAt;

    private OffsetDateTime readyAt;

    private OffsetDateTime expiresAt;

    private OffsetDateTime fulfilledAt;

    private OffsetDateTime cancelledAt;

    @Version
    private Long version = 0L;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

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