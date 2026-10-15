package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.domain.circulation.model.enums.LoanStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LoanResponse(
        UUID id,
        UUID patronId,
        String patronName,
        String patronExternalId,
        UUID itemId,
        String itemBarcode,
        String itemCallNumber,
        String workTitle,
        UUID libraryId,
        String libraryName,
        LoanStatus status,
        OffsetDateTime checkedOutAt,
        OffsetDateTime dueAt,
        OffsetDateTime returnedAt,
        UUID returnedLibraryId,
        Integer renewalCount,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
