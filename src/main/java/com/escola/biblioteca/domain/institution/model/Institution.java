package com.escola.biblioteca.domain.institution.model;

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

@Table("institution")
@Getter
@Setter
@NoArgsConstructor
public class Institution implements Persistable<UUID> {

    @Id
    private UUID id;

    private String code;

    private String name;

    private String settings = "{}";

    private OffsetDateTime createdAt;

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
    }
}