package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.model.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Resposta de login com token de acesso")
public record LoginResponse(
        @Schema(description = "JWT access token (validade 7 minutos)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "Nome do administrador", example = "Maria Admin")
        String nome,
        @Schema(description = "CPF do administrador (login)", example = "00000000000")
        String login,
        @Schema(description = "Role do administrador", example = "INSTITUTION_ADMIN")
        AdminRole role,
        @Schema(description = "UUID da instituição", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID institutionId) {
}
