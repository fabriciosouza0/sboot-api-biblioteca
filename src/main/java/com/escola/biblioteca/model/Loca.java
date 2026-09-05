package com.escola.biblioteca.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("loca")
@Getter
@Setter
@NoArgsConstructor
public class Loca {

    @Id
    private Integer codigo;

    private Long codigoLivro;

    private String cpfLocatario;

    private LocalDate dataDeLocacao;

    private LocalDate dataParaDevolucao;

    private boolean atrasado;
}
