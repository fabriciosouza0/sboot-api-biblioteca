package com.escola.biblioteca.repository.query;

import com.escola.biblioteca.dto.response.AutorResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AutorQueryRepository {

    private final JdbcClient jdbcClient;

    public AutorQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AutorResponse> buscarTodos(String nome) {
        if (nome == null) {
            return jdbcClient.sql("SELECT codigo, nome FROM autor ORDER BY nome ASC")
                    .query(this::mapAutor)
                    .list();
        }

        String sql = """
                SELECT codigo, nome FROM autor
                WHERE to_tsvector('portuguese', nome) @@ websearch_to_tsquery('portuguese', :nome || ':*')
                ORDER BY nome ASC
                """;

        return jdbcClient.sql(sql)
                .param("nome", nome)
                .query(this::mapAutor)
                .list();
    }

    private AutorResponse mapAutor(ResultSet rs, int rowNum) throws SQLException {
        return new AutorResponse(
                rs.getInt("codigo"),
                rs.getString("nome"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
