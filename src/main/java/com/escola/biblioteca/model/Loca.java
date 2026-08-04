package com.escola.biblioteca.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loca")
@Getter
@Setter
@NoArgsConstructor
public class Loca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_livro", nullable = false)
    private Livro livro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpfLocatario", nullable = false)
    private Locatario locatario;

    @Column(name = "dataDeLocacao", nullable = false)
    private LocalDate dataDeLocacao;

    @Column(name = "dataParaDevolucao", nullable = false)
    private LocalDate dataParaDevolucao;

    @Column(name = "atrasado", length = 1, nullable = false)
    private String atrasado;
}