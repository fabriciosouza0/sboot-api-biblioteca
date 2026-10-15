package com.escola.biblioteca.domain.circulation.repository;

import com.escola.biblioteca.domain.circulation.model.enums.FineStatus;
import com.escola.biblioteca.dto.response.FineResponse;
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
public class FineQueryRepository {

    private final PagedQueryHelper pagedQueryHelper;

    public FineQueryRepository(PagedQueryHelper pagedQueryHelper) {
        this.pagedQueryHelper = pagedQueryHelper;
    }

    public Page<FineResponse> findAllWithDetails(FilterWrapper filters, Pageable pageable) {
        UUID patronId = filters.get("patronId", UUID.class).orElse(null);
        String status = filters.getString("status").orElse(null);
        String term = filters.getString("term").orElse(null);
        return findAllWithDetails(patronId, status, term, pageable);
    }

    public Page<FineResponse> findAllWithDetails(
            UUID patronId, String status, String term,
            Pageable pageable) {

        StringBuilder fromAndWhere = new StringBuilder("""
            FROM fine f
            LEFT JOIN patron p ON p.id = f.patron_id
            LEFT JOIN loan lo ON lo.id = f.loan_id
            LEFT JOIN item i ON i.id = lo.item_id
            LEFT JOIN work w ON w.id = i.work_id
            WHERE 1=1
            """);

        Map<String, Object> params = new HashMap<>();
        if (patronId != null) {
            fromAndWhere.append(" AND f.patron_id = :patronId");
            params.put("patronId", patronId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            fromAndWhere.append(" AND f.status = :status");
            params.put("status", status.trim());
        }
        if (term != null && !term.isBlank()) {
            fromAndWhere.append(" AND (p.name ILIKE :termLike OR p.external_id ILIKE :termLike OR f.reason ILIKE :termLike OR w.title ILIKE :termLike OR i.barcode ILIKE :termLike)");
            params.put("termLike", "%" + term.trim() + "%");
        }

        String selectSql = """
            SELECT f.id, f.patron_id, p.name AS patron_name, p.external_id AS patron_external_id,
                   f.loan_id, i.barcode AS item_barcode, w.title AS work_title,
                   f.type, f.amount_cents, f.balance_cents, f.status, f.reason,
                   f.assessed_at, f.paid_at, f.version, f.created_at, f.updated_at
            """;

        return pagedQueryHelper.paginate(selectSql, fromAndWhere.toString(), params, "f.assessed_at DESC", pageable, FineResponse.class);
    }

    public List<FineResponse> findAllWithDetails(UUID patronId, String status, String term) {
        return findAllWithDetails(patronId, status, term, Pageable.unpaged()).getContent();
    }
}
