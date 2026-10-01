package com.escola.biblioteca.dto.request;

public record WorkRequest(
        String isbn13,
        String title,
        String authors,
        String publisher,
        Integer publishedYear,
        String edition,
        String cdu,
        String coverUrl,
        String description
) {}
