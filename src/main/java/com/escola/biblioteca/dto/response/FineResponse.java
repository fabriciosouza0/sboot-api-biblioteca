package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.domain.circulation.model.enums.FineStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FineResponse(
        UUID id,
        UUID patronId,
        String patronName,
        String patronExternalId,
        UUID loanId,
        String itemBarcode,
        String workTitle,
        String type,
        Integer amountCents,
        Integer balanceCents,
        FineStatus status,
        String reason,
        OffsetDateTime assessedAt,
        OffsetDateTime paidAt,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
