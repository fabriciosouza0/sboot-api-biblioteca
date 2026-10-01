package com.escola.biblioteca.domain.catalog.repository;

import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ItemQueryRepository {

    private final JdbcClient jdbcClient;

    public ItemQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
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
        item.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return item;
    }
}