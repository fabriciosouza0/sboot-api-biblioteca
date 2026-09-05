package com.escola.biblioteca.domain.repository.query;

import com.escola.biblioteca.domain.model.Patron;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PatronQueryRepository {

    private final JdbcClient jdbcClient;

    public PatronQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Patron> findByInstitutionIdAndExternalId(UUID institutionId, String externalId) {
        String sql = """
            SELECT p.id, p.institution_id, p.external_id, p.name, p.phone, p.profile, p.status, p.fine_balance, p.version, p.created_at, p.updated_at
            FROM patron p
            WHERE p.institution_id = :institutionId::text AND p.external_id = :externalId
            """;
        return jdbcClient.sql(sql)
                .param("institutionId", institutionId)
                .param("externalId", externalId)
                .query(this::mapPatron)
                .optional();
    }

    public List<Patron> searchByName(UUID institutionId, String term) {
        String sql = """
            SELECT p.id, p.institution_id, p.external_id, p.name, p.phone, p.profile, p.status, p.fine_balance, p.version, p.created_at, p.updated_at
            FROM patron p
            WHERE p.institution_id = :institutionId
              AND (:term IS NULL OR to_tsvector('portuguese', p.name) @@ websearch_to_tsquery('portuguese', :term || ':*'))
            ORDER BY p.name ASC
            """;
        String param = blankToNull(term);
        return jdbcClient.sql(sql)
                .param("institutionId", institutionId)
                .param("term", param)
                .query(this::mapPatron)
                .list();
    }

    private Patron mapPatron(ResultSet rs, int rowNum) throws SQLException {
        Patron patron = new Patron();
        patron.setId((UUID) rs.getObject("id"));
        patron.setInstitutionId((UUID) rs.getObject("institution_id"));
        patron.setExternalId(rs.getString("external_id"));
        patron.setName(rs.getString("name"));
        patron.setPhone(rs.getString("phone"));
        patron.setProfile(com.escola.biblioteca.domain.model.PatronProfile.valueOf(rs.getString("profile")));
        patron.setStatus(com.escola.biblioteca.domain.model.PatronStatus.valueOf(rs.getString("status")));
        patron.setFineBalance((Integer) rs.getObject("fine_balance"));
        patron.setVersion(rs.getLong("version"));
        patron.setCreatedAt(rs.getObject("created_at", java.time.Instant.class));
        patron.setUpdatedAt(rs.getObject("updated_at", java.time.Instant.class));
        return patron;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}