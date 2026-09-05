package com.escola.biblioteca.repository.query;

import com.escola.biblioteca.dto.response.CddResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CddQueryRepository {

    private final JdbcClient jdbcClient;

    public CddQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CddResponse> buscarTodos(String descricao) {
        if (descricao == null) {
            return jdbcClient.sql("SELECT codigo, descricao FROM cdd ORDER BY descricao ASC")
                    .query(this::mapCdd)
                    .list();
        }
        String sql = """
                SELECT codigo, descricao FROM cdd
                WHERE to_tsvector('portuguese', descricao) @@ websearch_to_tsquery('portuguese', :descricao || ':*')
                ORDER BY descricao ASC
                """;
        return jdbcClient.sql(sql)
                .param("descricao", descricao)
                .query(this::mapCdd)
                .list();
    }

    private CddResponse mapCdd(ResultSet rs, int rowNum) throws SQLException {
        return new CddResponse(
                rs.getLong("codigo"),
                rs.getString("descricao"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}