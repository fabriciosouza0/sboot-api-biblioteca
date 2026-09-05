package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("turma")
@Getter
@Setter
@NoArgsConstructor
public class Turma {

    @Id
    private Integer codigo;

    private String descricao;

    public Turma(Integer codigo) {
        this.codigo = codigo;
    }
}
