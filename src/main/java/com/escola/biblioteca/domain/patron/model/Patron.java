package com.escola.biblioteca.domain.patron.model;

import com.escola.biblioteca.domain.patron.model.enums.PatronProfile;
import com.escola.biblioteca.domain.patron.model.enums.PatronStatus;

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

@Table("patron")
@Getter
@Setter
@NoArgsConstructor
public class Patron implements Persistable<UUID> {

    @Id
    private UUID id;

    private UUID institutionId;

    private String externalId;

    private String name;

    private String phone;

    private PatronProfile profile;

    private PatronStatus status = PatronStatus.ACTIVE;

    private Integer fineBalance = 0;

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
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }
}