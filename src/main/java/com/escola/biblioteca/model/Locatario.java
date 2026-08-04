package com.escola.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "locatario")
@Getter
@Setter
@NoArgsConstructor
public class Locatario {

    @Id
    @Column(name = "CPF", length = 14)
    private String cpf;

    @Column(name = "NOME", length = 45, nullable = false)
    private String nome;

    @Column(name = "telefone", length = 14)
    private String telefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODIGO_PROFESSOR")
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODIGO_ALUNO")
    private Aluno aluno;
}