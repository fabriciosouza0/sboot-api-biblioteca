package com.escola.biblioteca.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("professor")
@Getter
@Setter
@NoArgsConstructor
public class Professor {

    @Id
    private Integer codigo;
}
