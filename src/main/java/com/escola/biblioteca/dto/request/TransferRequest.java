package com.escola.biblioteca.dto.request;

import java.util.UUID;

public record TransferRequest(
        UUID targetLibraryId
) {}
