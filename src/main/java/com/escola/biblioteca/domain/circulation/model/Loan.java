package com.escola.biblioteca.domain.circulation.model;

import com.escola.biblioteca.domain.circulation.model.enums.LoanStatus;

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

    private OffsetDateTime checkedOutAt;

    private OffsetDateTime dueAt;

    private OffsetDateTime returnedAt;

    private UUID returnedLibraryId;

    private Integer renewalCount = 0;

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