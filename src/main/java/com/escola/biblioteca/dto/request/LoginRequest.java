package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{validation.login.login}") String login,
        @NotBlank(message = "{validation.login.senha}") String senha) {
}
