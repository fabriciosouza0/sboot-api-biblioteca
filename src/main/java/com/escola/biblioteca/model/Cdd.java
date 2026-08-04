package com.escola.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cdd")
@Getter
@Setter
@NoArgsConstructor
public class Cdd {

    @Id
    @Column(name = "CODIGO")
    private Long id;

    @Column(name = "DESCRICAO", length = 45, nullable = false)
    private String descricao;
}