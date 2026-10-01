package com.escola.biblioteca.dto.request;

public record PatronRequest(
        String externalId,
        String name,
        String phone,
        String profile
) {}
