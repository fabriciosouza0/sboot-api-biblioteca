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

@Table("item")
@Getter
@Setter
@NoArgsConstructor
public class Item implements Persistable<UUID> {

    @Id
    private UUID id;

    private UUID workId;

    private UUID libraryId;

    private String barcode;

    private String callNumber;

    private ItemStatus status = ItemStatus.AVAILABLE;

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