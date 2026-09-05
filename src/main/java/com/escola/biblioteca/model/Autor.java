package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("autor")
@Getter
@Setter
@NoArgsConstructor
public class Autor {

    @Id
    private Integer codigo;

    private String nome;
}
