package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AutorRequest(
        @NotBlank(message = "{validation.autor.nome}")
        @Size(max = 45, message = "{validation.size}")
        String nome) {
}
