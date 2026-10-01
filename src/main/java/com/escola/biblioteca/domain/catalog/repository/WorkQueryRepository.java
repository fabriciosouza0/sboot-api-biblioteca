package com.escola.biblioteca.domain.catalog.repository;

import com.escola.biblioteca.domain.catalog.model.Work;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WorkQueryRepository {

    private final JdbcClient jdbcClient;

    public WorkQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Work> searchByTitle(UUID institutionId, String term) {
        String sql = """
            SELECT w.id, w.institution_id, w.isbn13, w.title, w.authors, w.publisher, w.published_year, w.edition, w.cdu, w.cover_url, w.description, w.version, w.created_at, w.updated_at
            FROM work w
            WHERE (w.institution_id = :institutionId OR w.institution_id IS NULL)
              AND (:term::text IS NULL OR to_tsvector('portuguese', w.title) @@ websearch_to_tsquery('portuguese', :term::text || ':*'))
            ORDER BY w.title ASC
            """;
        String param = blankToNull(term);
        return jdbcClient.sql(sql)
                .param("institutionId", institutionId)
                .param("term", param)
                .query(this::mapWork)
                .list();
    }

    public Optional<Work> findByInstitutionIdAndIsbn13(UUID institutionId, String isbn13) {
        String sql = """
            SELECT w.id, w.institution_id, w.isbn13, w.title, w.authors, w.publisher, w.published_year, w.edition, w.cdu, w.cover_url, w.description, w.version, w.created_at, w.updated_at
            FROM work w
            WHERE (w.institution_id = :institutionId OR w.institution_id IS NULL) AND w.isbn13 = :isbn13
            """;
        return jdbcClient.sql(sql)
                .param("institutionId", institutionId)
                .param("isbn13", isbn13)
                .query(this::mapWork)
                .optional();
    }

    private Work mapWork(ResultSet rs, int rowNum) throws SQLException {
        Work work = new Work();
        work.setId((UUID) rs.getObject("id"));
        work.setInstitutionId((UUID) rs.getObject("institution_id"));
        work.setIsbn13(rs.getString("isbn13"));
        work.setTitle(rs.getString("title"));
        work.setAuthors(rs.getString("authors"));
        work.setPublisher(rs.getString("publisher"));
        work.setPublishedYear((Integer) rs.getObject("published_year"));
        work.setEdition(rs.getString("edition"));
        work.setCdu(rs.getString("cdu"));
        work.setCoverUrl(rs.getString("cover_url"));
        work.setDescription(rs.getString("description"));
        work.setVersion(rs.getLong("version"));
        work.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
        work.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
        return work;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}