package com.escola.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um livro")
public record LivroResponse(
        @Schema(description = "ISBN do livro", example = "9788535212345")
        Long codigo,
        @Schema(description = "Título do livro", example = "Clean Code")
        String titulo,
        @Schema(description = "Quantidade de exemplares disponíveis", example = "3")
        Integer qtd,
        @Schema(description = "Código do autor", example = "1")
        Integer codigoAutor,
        @Schema(description = "Nome do autor", example = "Robert C. Martin")
        String autor,
        @Schema(description = "Código CDD", example = "510")
        Long codigoCDD,
        @Schema(description = "Descrição da classificação CDD", example = "Matemática")
        String cdd) {
}
