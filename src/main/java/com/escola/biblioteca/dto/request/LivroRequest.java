package com.escola.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Requisição para criar ou atualizar um livro")
public record LivroRequest(
        @NotNull(message = "{validation.livro.codigo}")
        @Digits(integer = 13, fraction = 0, message = "{validation.digits}")
        @Schema(description = "ISBN do livro (até 13 dígitos)", example = "9788535212345")
        Long codigo,
        @NotBlank(message = "{validation.livro.titulo}")
        @Size(max = 45, message = "{validation.size}")
        @Schema(description = "Título do livro (máx. 45 caracteres)", example = "Clean Code")
        String titulo,
        @NotNull(message = "{validation.livro.qtd}")
        @Min(value = 0, message = "{validation.livro.qtd.min}")
        @Schema(description = "Quantidade de exemplares", example = "3")
        Integer qtd,
        @NotNull(message = "{validation.livro.autor}")
        @Schema(description = "Código do autor (FK)", example = "1")
        Integer codigoAutor,
        @NotNull(message = "{validation.livro.cdd}")
        @Schema(description = "Código CDD (Classificação Decimal Dewey)", example = "510")
        Long codigoCDD) {
}
