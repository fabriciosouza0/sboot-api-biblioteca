package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("aluno")
@Getter
@Setter
@NoArgsConstructor
public class Aluno {

    @Id
    private Integer codigo;

    private Integer codigoTurma;

    public Aluno(Integer codigoTurma) {
        this.codigoTurma = codigoTurma;
    }
}
