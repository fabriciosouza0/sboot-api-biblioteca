package com.escola.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Resposta com dados de uma locação")
public record LocacaoResponse(
        @Schema(description = "Código sequencial da locação", example = "1")
        Integer codigo,
        @Schema(description = "ISBN do livro locado", example = "9788535212345")
        Long codigoLivro,
        @Schema(description = "Título do livro", example = "Clean Code")
        String livro,
        @Schema(description = "CPF do locatário", example = "12345678900")
        String cpfLocatario,
        @Schema(description = "Nome do locatário", example = "João da Silva")
        String locatario,
        @Schema(description = "Data da locação", example = "2026-08-01")
        LocalDate dataDeLocacao,
        @Schema(description = "Data prevista para devolução", example = "2026-08-08")
        LocalDate dataParaDevolucao,
        @Schema(description = "Indica se a locação está atrasada", example = "false")
        boolean atrasado,
        @Schema(description = "Dias desde a locação", example = "3")
        long dias) {
}
