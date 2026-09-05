package com.escola.biblioteca.repository.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LocacaoQueryRepository {

    public record LocacaoRow(
            Integer codigo,
            Long codigoLivro,
            String livro,
            String cpfLocatario,
            String locatario,
            LocalDate dataDeLocacao,
            LocalDate dataParaDevolucao) {
    }

    private final JdbcClient jdbcClient;

    public LocacaoQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<LocacaoRow> buscarTodas(String locatario) {
        String sql = """
                SELECT lc.codigo, lc.codigo_livro, lv.titulo AS livro, lc.cpf_locatario, lt.nome AS locatario,
                       lc.data_de_locacao, lc.data_para_devolucao
                FROM loca lc
                JOIN livro lv ON lv.codigo = lc.codigo_livro
                JOIN locatario lt ON lt.cpf = lc.cpf_locatario
                WHERE (:locatario::text IS NULL
                       OR to_tsvector('portuguese', lt.nome) @@ websearch_to_tsquery('portuguese', :locatario || ':*'))
                ORDER BY lc.data_de_locacao DESC
                """;

        return jdbcClient.sql(sql)
                .param("locatario", blankToNull(locatario))
                .query(this::mapRow)
                .list();
    }

    private LocacaoRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LocacaoRow(
                (Integer) rs.getObject("codigo"),
                rs.getLong("codigo_livro"),
                rs.getString("livro"),
                rs.getString("cpf_locatario"),
                rs.getString("locatario"),
                rs.getObject("data_de_locacao", LocalDate.class),
                rs.getObject("data_para_devolucao", LocalDate.class));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}