package com.escola.biblioteca.repository.query;

import com.escola.biblioteca.dto.response.LivroResponse;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LivroQueryRepository {

    private final JdbcClient jdbcClient;

    public LivroQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<LivroResponse> buscarTodos(String titulo) {
        String sql = """
                SELECT l.codigo, l.titulo, l.qtd, l.codigo_autor, a.nome AS autor,
                       l.codigo_cdd, c.descricao AS cdd
                FROM livro l
                LEFT JOIN autor a ON a.codigo = l.codigo_autor
                LEFT JOIN cdd c ON c.codigo = l.codigo_cdd
                WHERE (:titulo IS NULL
                       OR to_tsvector('portuguese', l.titulo) @@ websearch_to_tsquery('portuguese', :titulo || ':*'))
                ORDER BY l.titulo ASC
                """;
        String param = blankToNull(titulo);
        return jdbcClient.sql(sql)
                .param("titulo", param, Types.VARCHAR)
                .query(this::mapLivro)
                .list();
    }

    public Optional<LivroResponse> buscarPorCodigo(Long codigo) {
        String sql = """
                SELECT l.codigo, l.titulo, l.qtd, l.codigo_autor, a.nome AS autor,
                       l.codigo_cdd, c.descricao AS cdd
                FROM livro l
                LEFT JOIN autor a ON a.codigo = l.codigo_autor
                LEFT JOIN cdd c ON c.codigo = l.codigo_cdd
                WHERE l.codigo = :codigo
                """;
        return jdbcClient.sql(sql)
                .param("codigo", codigo)
                .query((rs, rowNum) -> new LivroResponse(
                        rs.getLong("codigo"),
                        rs.getString("titulo"),
                        (Integer) rs.getObject("qtd"),
                        (Integer) rs.getObject("codigo_autor"),
                        rs.getString("autor"),
                        (Long) rs.getObject("codigo_cdd"),
                        rs.getString("cdd")))
                .optional();
    }

    private LivroResponse mapLivro(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new LivroResponse(
                rs.getLong("codigo"),
                rs.getString("titulo"),
                (Integer) rs.getObject("qtd"),
                (Integer) rs.getObject("codigo_autor"),
                rs.getString("autor"),
                (Long) rs.getObject("codigo_cdd"),
                rs.getString("cdd"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}