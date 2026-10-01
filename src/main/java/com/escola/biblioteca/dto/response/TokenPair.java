package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.model.enums.AdminRole;
import java.util.UUID;

/**
 * Par de tokens retornado internamente pelo AuthService.
 * O refresh token nunca viaja no corpo: vai apenas no cookie httpOnly.
 */
public record TokenPair(String accessToken, String refreshToken, String nome, String login,
                        AdminRole role, UUID institutionId) {
}
