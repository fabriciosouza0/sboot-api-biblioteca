package com.escola.biblioteca.dto.response;

import com.escola.biblioteca.repository.query.FilterWrapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record PagedResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty,
        Map<String, Object> appliedFilters
) {
    public static <T> PagedResponse<T> of(Page<T> page, FilterWrapper filterWrapper) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                (filterWrapper != null) ? filterWrapper.filters() : Map.of()
        );
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return of(page, FilterWrapper.empty());
    }
}
