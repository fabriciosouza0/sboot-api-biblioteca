package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        UUID workId,
        String workTitle,
        String workIsbn13,
        UUID libraryId,
        String libraryName,
        String barcode,
        String callNumber,
        ItemStatus status,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String transferNote
) {}
