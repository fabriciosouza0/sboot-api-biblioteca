package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CddUpdateRequest(
        @NotBlank(message = "{validation.cdd.descricao}")
        @Size(max = 45, message = "{validation.size}")
        String descricao) {
}
