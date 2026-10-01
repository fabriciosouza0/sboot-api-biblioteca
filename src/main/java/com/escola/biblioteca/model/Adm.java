package com.escola.biblioteca.model;

import com.escola.biblioteca.model.enums.AdminRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

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

    private AdminRole role;

    private UUID institutionId;
}
