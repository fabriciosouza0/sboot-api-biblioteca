package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("locatario")
@Getter
@Setter
@NoArgsConstructor
public class Locatario implements Persistable<String> {

    @Id
    private String cpf;

    private String nome;

    private String telefone;

    private Integer codigoProfessor;

    private Integer codigoAluno;

    @Transient
    private boolean novo;

    @Override
    public String getId() {
        return cpf;
    }

    @Override
    public boolean isNew() {
        return novo;
    }

    public void marcarNovo() {
        this.novo = true;
    }
}