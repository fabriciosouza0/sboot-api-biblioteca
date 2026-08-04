package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LivroRequest(
        @NotNull(message = "{validation.livro.codigo}")
        @Digits(integer = 13, fraction = 0, message = "{validation.digits}") Long codigo,
        @NotBlank(message = "{validation.livro.titulo}")
        @Size(max = 45, message = "{validation.size}") String titulo,
        @NotNull(message = "{validation.livro.qtd}") @Min(value = 0, message = "{validation.livro.qtd.min}") Integer qtd,
        @NotNull(message = "{validation.livro.autor}") Integer codigoAutor,
        @NotNull(message = "{validation.livro.cdd}") Long codigoCDD) {
}
