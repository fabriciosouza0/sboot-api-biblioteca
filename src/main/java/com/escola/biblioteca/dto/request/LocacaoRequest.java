package com.escola.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Requisição para registrar uma locação")
public record LocacaoRequest(
        @NotNull(message = "{validation.locacao.livro}")
        @Schema(description = "Código (ISBN) do livro a ser locado", example = "9788535212345")
        Long codigoLivro,
        @NotBlank(message = "{validation.locacao.locatario}")
        @Pattern(regexp = "\\d{11}", message = "{validation.cpf.invalid}")
        @Schema(description = "CPF do locatário (somente dígitos, 11 caracteres)", example = "12345678900")
        String cpfLocatario,
        @NotNull(message = "{validation.locacao.dias}")
        @Min(value = 1, message = "{validation.locacao.dias.min}")
        @Schema(description = "Número de dias para devolução", example = "7")
        Integer dias) {
}
