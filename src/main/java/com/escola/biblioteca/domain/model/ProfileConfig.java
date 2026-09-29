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

@Table("profile_config")
@Getter
@Setter
@NoArgsConstructor
public class ProfileConfig implements Persistable<UUID> {

    @Id
    private UUID id;

    private UUID institutionId;

    private PatronProfile profile;

    private Integer maxLoans;

    private Integer loanDays;

    private Integer maxRenewals;

    private Integer holdLimit;

    private Integer fineRateCents = 50;

    private Integer fineCapCents = 5000;

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