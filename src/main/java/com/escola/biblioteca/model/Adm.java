package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("adms")
@Getter
@Setter
@NoArgsConstructor
public class Adm {

    @Id
    private Integer codigo;

    private String login;

    private String senha;

    private String nome;
}
