package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LocacaoRequest(
        @NotNull(message = "{validation.locacao.livro}") Long codigoLivro,
        @NotBlank(message = "{validation.locacao.locatario}")
        @Pattern(regexp = "\\d{11}", message = "{validation.cpf.invalid}") String cpfLocatario,
        @NotNull(message = "{validation.locacao.dias}") @Min(value = 1, message = "{validation.locacao.dias.min}") Integer dias) {
}
