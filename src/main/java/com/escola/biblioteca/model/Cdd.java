package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("cdd")
@Getter
@Setter
@NoArgsConstructor
public class Cdd implements Persistable<Long> {

    @Id
    private Long codigo;

    private String descricao;

    @Transient
    private boolean novo;

    @Override
    public Long getId() {
        return codigo;
    }

    @Override
    public boolean isNew() {
        return novo;
    }

    public void marcarNovo() {
        this.novo = true;
    }
}
