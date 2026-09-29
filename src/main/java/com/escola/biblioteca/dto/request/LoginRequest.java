package com.escola.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição de login")
public record LoginRequest(
        @NotBlank(message = "{validation.login.login}")
        @Schema(description = "CPF do administrador (somente dígitos)", example = "00000000000")
        String login,
        @NotBlank(message = "{validation.login.senha}")
        @Schema(description = "Senha do administrador", example = "admin")
        String senha) {
}
