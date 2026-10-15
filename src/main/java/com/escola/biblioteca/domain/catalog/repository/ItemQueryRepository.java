package com.escola.biblioteca.domain.catalog.repository;

import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import com.escola.biblioteca.dto.response.ItemResponse;
import com.escola.biblioteca.repository.query.FilterWrapper;
import com.escola.biblioteca.repository.query.PagedQueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ItemQueryRepository {

    private final JdbcClient jdbcClient;
    private final PagedQueryHelper pagedQueryHelper;

    public ItemQueryRepository(JdbcClient jdbcClient, PagedQueryHelper pagedQueryHelper) {
        this.jdbcClient = jdbcClient;
        this.pagedQueryHelper = pagedQueryHelper;
    }

    public List<Item> findAvailableByWorkAndLibrary(UUID workId, UUID libraryId) {
        String sql = """
            SELECT i.id, i.work_id, i.library_id, i.barcode, i.call_number, i.status, i.version, i.created_at, i.updated_at
            FROM item i
            WHERE i.work_id = :workId::text AND i.library_id = :libraryId AND i.status = :status
            """;
        return jdbcClient.sql(sql)
                .param("workId", workId)
                .param("libraryId", libraryId)
                .param("status", ItemStatus.AVAILABLE.name())
                .query(this::mapItem)
                .list();
    }

    public Optional<Item> findByBarcode(String barcode) {
        String sql = """
            SELECT i.id, i.work_id, i.library_id, i.barcode, i.call_number, i.status, i.version, i.created_at, i.updated_at
            FROM item i
            WHERE i.barcode = :barcode::text
            """;
        return jdbcClient.sql(sql)
                .param("barcode", barcode)
                .query(this::mapItem)
                .optional();
    }

    private Item mapItem(ResultSet rs, int rowNum) throws SQLException {
        Item item = new Item();
        item.setId((UUID) rs.getObject("id"));
        item.setWorkId((UUID) rs.getObject("work_id"));
        item.setLibraryId((UUID) rs.getObject("library_id"));
        item.setBarcode(rs.getString("barcode"));
        item.setCallNumber(rs.getString("call_number"));
        item.setStatus(ItemStatus.valueOf(rs.getString("status")));
        item.setVersion(rs.getLong("version"));
        item.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return item;
    }

    public Page<ItemResponse> findAllWithDetails(FilterWrapper filters, Pageable pageable) {
        UUID workId = filters.get("workId", UUID.class).orElse(null);
        UUID libraryId = filters.get("libraryId", UUID.class).orElse(null);
        ItemStatus status = filters.get("status", ItemStatus.class).orElse(null);
        String term = filters.getString("term").orElse(null);
        return findAllWithDetails(workId, libraryId, status, term, pageable);
    }

    public Page<ItemResponse> findAllWithDetails(
            UUID workId, UUID libraryId, ItemStatus status, String term,
            Pageable pageable) {

        StringBuilder fromAndWhere = new StringBuilder("""
            FROM item i
            LEFT JOIN work w ON w.id = i.work_id
            LEFT JOIN library l ON l.id = i.library_id
            WHERE 1=1
            """);

        Map<String, Object> params = new HashMap<>();
        if (workId != null) {
            fromAndWhere.append(" AND i.work_id = :workId");
            params.put("workId", workId);
        }
        if (libraryId != null) {
            fromAndWhere.append(" AND i.library_id = :libraryId");
            params.put("libraryId", libraryId);
        }
        if (status != null) {
            fromAndWhere.append(" AND i.status = :status");
            params.put("status", status.name());
        }
        if (term != null && !term.isBlank()) {
            fromAndWhere.append(" AND (i.barcode ILIKE :termLike OR i.call_number ILIKE :termLike OR w.title ILIKE :termLike)");
            params.put("termLike", "%" + term.trim() + "%");
        }

        String selectSql = """
            SELECT i.id, i.work_id, w.title AS work_title, w.isbn13 AS work_isbn13,
                   i.library_id, l.name AS library_name,
                   i.barcode, i.call_number, i.status, i.version, i.created_at, i.updated_at,
                   i.transfer_note
            """;

        return pagedQueryHelper.paginate(selectSql, fromAndWhere.toString(), params, "i.created_at DESC", pageable, ItemResponse.class);
    }

    public List<ItemResponse> findAllWithDetails(UUID workId, UUID libraryId, ItemStatus status, String term) {
        return findAllWithDetails(workId, libraryId, status, term, Pageable.unpaged()).getContent();
    }
}