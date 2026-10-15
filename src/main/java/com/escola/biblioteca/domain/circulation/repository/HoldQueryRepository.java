package com.escola.biblioteca.domain.circulation.repository;

import com.escola.biblioteca.domain.circulation.model.enums.HoldStatus;
import com.escola.biblioteca.dto.response.HoldResponse;
import com.escola.biblioteca.repository.query.FilterWrapper;
import com.escola.biblioteca.repository.query.PagedQueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class HoldQueryRepository {

    private final PagedQueryHelper pagedQueryHelper;

    public HoldQueryRepository(PagedQueryHelper pagedQueryHelper) {
        this.pagedQueryHelper = pagedQueryHelper;
    }

    public Page<HoldResponse> findAllWithDetails(FilterWrapper filters, Pageable pageable) {
        UUID patronId = filters.get("patronId", UUID.class).orElse(null);
        String status = filters.getString("status").orElse(null);
        String term = filters.getString("term").orElse(null);
        return findAllWithDetails(patronId, status, term, pageable);
    }

    public Page<HoldResponse> findAllWithDetails(
            UUID patronId, String status, String term,
            Pageable pageable) {

        StringBuilder fromAndWhere = new StringBuilder("""
            FROM hold h
            LEFT JOIN patron p ON p.id = h.patron_id
            LEFT JOIN work w ON w.id = h.work_id
            LEFT JOIN library l ON l.id = h.library_id
            WHERE 1=1
            """);

        Map<String, Object> params = new HashMap<>();
        if (patronId != null) {
            fromAndWhere.append(" AND h.patron_id = :patronId");
            params.put("patronId", patronId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            fromAndWhere.append(" AND h.status = :status");
            params.put("status", status.trim());
        }
        if (term != null && !term.isBlank()) {
            fromAndWhere.append(" AND (p.name ILIKE :termLike OR p.external_id ILIKE :termLike OR w.title ILIKE :termLike OR l.name ILIKE :termLike)");
            params.put("termLike", "%" + term.trim() + "%");
        }

        String selectSql = """
            SELECT h.id, h.patron_id, p.name AS patron_name, p.external_id AS patron_external_id,
                   h.work_id, w.title AS work_title, w.isbn13 AS work_isbn13,
                   h.library_id, l.name AS library_name,
                   h.status, h.position, h.placed_at, h.ready_at, h.expires_at, h.fulfilled_at, h.cancelled_at,
                   h.version, h.created_at, h.updated_at
            """;

        return pagedQueryHelper.paginate(selectSql, fromAndWhere.toString(), params, "h.placed_at DESC", pageable, HoldResponse.class);
    }

    public List<HoldResponse> findAllWithDetails(UUID patronId, String status, String term) {
        return findAllWithDetails(patronId, status, term, Pageable.unpaged()).getContent();
    }
}
