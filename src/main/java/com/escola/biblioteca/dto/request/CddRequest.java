package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CddRequest(
        @NotNull(message = "{validation.cdd.codigo}") Long id,
        @NotBlank(message = "{validation.cdd.descricao}")
        @Size(max = 45, message = "{validation.size}")
        String descricao) {
}
