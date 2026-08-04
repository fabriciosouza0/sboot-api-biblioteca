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
@Table(name = "livro")
@Getter
@Setter
@NoArgsConstructor
public class Livro {

    @Id
    @Column(name = "codigo")
    private Long id;

    @Column(name = "titulo", length = 45, nullable = false)
    private String titulo;

    @Column(name = "qtd")
    private Integer qtd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_autor")
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_cdd")
    private Cdd cdd;
}