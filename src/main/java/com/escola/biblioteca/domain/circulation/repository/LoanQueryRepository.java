package com.escola.biblioteca.domain.circulation.repository;

import com.escola.biblioteca.domain.circulation.model.enums.LoanStatus;
import com.escola.biblioteca.dto.response.LoanResponse;
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
public class LoanQueryRepository {

    private final PagedQueryHelper pagedQueryHelper;

    public LoanQueryRepository(PagedQueryHelper pagedQueryHelper) {
        this.pagedQueryHelper = pagedQueryHelper;
    }

    public Page<LoanResponse> findAllWithDetails(FilterWrapper filters, Pageable pageable) {
        UUID patronId = filters.get("patronId", UUID.class).orElse(null);
        String status = filters.getString("status").orElse(null);
        String term = filters.getString("term").orElse(null);
        return findAllWithDetails(patronId, status, term, pageable);
    }

    public Page<LoanResponse> findAllWithDetails(
            UUID patronId, String status, String term,
            Pageable pageable) {

        StringBuilder fromAndWhere = new StringBuilder("""
            FROM loan lo
            LEFT JOIN patron p ON p.id = lo.patron_id
            LEFT JOIN item i ON i.id = lo.item_id
            LEFT JOIN work w ON w.id = i.work_id
            LEFT JOIN library l ON l.id = lo.library_id
            WHERE 1=1
            """);

        Map<String, Object> params = new HashMap<>();
        if (patronId != null) {
            fromAndWhere.append(" AND lo.patron_id = :patronId");
            params.put("patronId", patronId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            fromAndWhere.append(" AND lo.status = :status");
            params.put("status", status.trim());
        }
        if (term != null && !term.isBlank()) {
            fromAndWhere.append(" AND (p.name ILIKE :termLike OR p.external_id ILIKE :termLike OR i.barcode ILIKE :termLike OR w.title ILIKE :termLike)");
            params.put("termLike", "%" + term.trim() + "%");
        }

        String selectSql = """
            SELECT lo.id, lo.patron_id, p.name AS patron_name, p.external_id AS patron_external_id,
                   lo.item_id, i.barcode AS item_barcode, i.call_number AS item_call_number,
                   w.title AS work_title,
                   lo.library_id, l.name AS library_name,
                   lo.status, lo.checked_out_at, lo.due_at, lo.returned_at, lo.returned_library_id,
                   lo.renewal_count, lo.version, lo.created_at, lo.updated_at
            """;

        return pagedQueryHelper.paginate(selectSql, fromAndWhere.toString(), params, "lo.checked_out_at DESC", pageable, LoanResponse.class);
    }

    public List<LoanResponse> findAllWithDetails(UUID patronId, String status, String term) {
        return findAllWithDetails(patronId, status, term, Pageable.unpaged()).getContent();
    }
}
