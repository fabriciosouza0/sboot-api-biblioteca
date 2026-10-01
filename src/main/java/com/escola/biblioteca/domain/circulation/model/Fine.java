package com.escola.biblioteca.domain.circulation.model;

import com.escola.biblioteca.domain.circulation.model.enums.FineStatus;
import com.escola.biblioteca.domain.circulation.model.enums.FineType;

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

@Table("fine")
@Getter
@Setter
@NoArgsConstructor
public class Fine implements Persistable<UUID> {

    @Id
    private UUID id;

    private UUID patronId;

    private UUID loanId;

    private FineType type;

    private Integer amountCents;

    private Integer balanceCents;

    private FineStatus status = FineStatus.PENDING;

    private OffsetDateTime assessedAt;

    private OffsetDateTime paidAt;

    private OffsetDateTime waivedAt;

    private String reason;

    @Version
    private Long version = 0L;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

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