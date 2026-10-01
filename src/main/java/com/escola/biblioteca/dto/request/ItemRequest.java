package com.escola.biblioteca.dto.request;

import java.util.UUID;

public record ItemRequest(
        UUID workId,
        UUID libraryId,
        String barcode,
        String callNumber
) {}
