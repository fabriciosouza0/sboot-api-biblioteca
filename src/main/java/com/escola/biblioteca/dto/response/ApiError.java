package com.escola.biblioteca.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Erro padronizado da API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        @Schema(description = "Data/hora do erro (ISO-8601)", example = "2026-08-04T14:30:00")
        LocalDateTime timestamp,
        @Schema(description = "Código HTTP", example = "400")
        int status,
        @Schema(description = "Descrição do erro HTTP", example = "Bad Request")
        String error,
        @Schema(description = "Código interno do erro", example = "validation.error")
        String code,
        @Schema(description = "Mensagem amigável ao usuário", example = "Dados inválidos")
        String message,
        @Schema(description = "Caminho da requisição que gerou o erro", example = "/api/livros")
        String path,
        @Schema(description = "Erros de validação por campo (quando aplicável)")
        Map<String, String> errors) {
}
