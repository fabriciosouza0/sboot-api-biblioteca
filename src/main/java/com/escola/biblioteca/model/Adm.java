package com.escola.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adms")
@Getter
@Setter
@NoArgsConstructor
public class Adm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer id;

    @Column(name = "login", length = 15, nullable = false)
    private String login;

    @Column(name = "senha", length = 40, nullable = false)
    private String senha;

    @Column(name = "nome", length = 20, nullable = false)
    private String nome;
}