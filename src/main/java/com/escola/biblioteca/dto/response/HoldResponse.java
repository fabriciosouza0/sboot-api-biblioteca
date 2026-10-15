package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.domain.circulation.model.enums.HoldStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record HoldResponse(
        UUID id,
        UUID patronId,
        String patronName,
        String patronExternalId,
        UUID workId,
        String workTitle,
        String workIsbn13,
        UUID libraryId,
        String libraryName,
        HoldStatus status,
        Integer position,
        OffsetDateTime placedAt,
        OffsetDateTime readyAt,
        OffsetDateTime expiresAt,
        OffsetDateTime fulfilledAt,
        OffsetDateTime cancelledAt,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
