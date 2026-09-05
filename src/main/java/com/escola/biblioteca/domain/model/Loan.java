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

@Table("loan")
@Getter
@Setter
@NoArgsConstructor
public class Loan implements Persistable<UUID> {

    @Id
    private UUID id;

    private UUID patronId;

    private UUID itemId;

    private UUID libraryId;

    private LoanStatus status = LoanStatus.ACTIVE;

    private Instant checkedOutAt;

    private Instant dueAt;

    private Instant returnedAt;

    private UUID returnedLibraryId;

    private Integer renewalCount = 0;

    @Version
    private Long version = 0L;

    private Instant createdAt;

    private Instant updatedAt;

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