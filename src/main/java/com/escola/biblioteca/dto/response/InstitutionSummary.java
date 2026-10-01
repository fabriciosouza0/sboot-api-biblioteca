package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.model.enums.AdminRole;
import java.util.UUID;

public record InstitutionSummary(UUID id, String code, String name, AdminRole role) {
}