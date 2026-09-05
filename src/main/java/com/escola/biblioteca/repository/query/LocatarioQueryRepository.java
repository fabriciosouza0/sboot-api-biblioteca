package com.escola.biblioteca.repository.query;

import com.escola.biblioteca.dto.response.AlunoResponse;
import com.escola.biblioteca.dto.response.ProfessorResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LocatarioQueryRepository {

    private final JdbcClient jdbcClient;

    public LocatarioQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AlunoResponse> buscarAlunos(String nome) {
        String sql = """
                SELECT lt.cpf, lt.nome, lt.telefone, al.codigo, al.codigo_turma, t.descricao AS turma
                FROM locatario lt
                JOIN aluno al ON al.codigo = lt.codigo_aluno
                LEFT JOIN turma t ON t.codigo = al.codigo_turma
                WHERE (:nome::text IS NULL
                       OR to_tsvector('portuguese', lt.nome) @@ websearch_to_tsquery('portuguese', :nome || ':*'))
                ORDER BY lt.nome ASC
                """;

        return jdbcClient.sql(sql)
                .param("nome", blankToNull(nome))
                .query(this::mapAluno)
                .list();
    }

    public Optional<AlunoResponse> buscarAlunoPorCpf(String cpf) {
        String sql = """
                SELECT lt.cpf, lt.nome, lt.telefone, al.codigo, al.codigo_turma, t.descricao AS turma
                FROM locatario lt
                JOIN aluno al ON al.codigo = lt.codigo_aluno
                LEFT JOIN turma t ON t.codigo = al.codigo_turma
                WHERE lt.cpf = :cpf::text
                """;

        return jdbcClient.sql(sql)
                .param("cpf", blankToNull(cpf))
                .query(this::mapAluno)
                .optional();
    }

    public List<ProfessorResponse> buscarProfessores(String nome) {
        String sql = """
                SELECT lt.cpf, lt.nome, p.codigo
                FROM locatario lt
                JOIN professor p ON p.codigo = lt.codigo_professor
                WHERE (:nome::text IS NULL
                       OR to_tsvector('portuguese', lt.nome) @@ websearch_to_tsquery('portuguese', :nome || ':*'))
                ORDER BY lt.nome ASC
                """;

        return jdbcClient.sql(sql)
                .param("nome", blankToNull(nome))
                .query(this::mapProfessor)
                .list();
    }

    public Optional<ProfessorResponse> buscarProfessorPorCpf(String cpf) {
        String sql = """
                SELECT lt.cpf, lt.nome, p.codigo
                FROM locatario lt
                JOIN professor p ON p.codigo = lt.codigo_professor
                WHERE lt.cpf = :cpf
                """;

        return jdbcClient.sql(sql)
                .params("cpf", cpf)
                .query(this::mapProfessor)
                .optional();
    }

    private AlunoResponse mapAluno(ResultSet rs, int rowNum) throws SQLException {
        return new AlunoResponse(
                rs.getString("cpf"),
                rs.getString("nome"),
                rs.getString("telefone"),
                (Integer) rs.getObject("codigo"),
                (Integer) rs.getObject("codigo_turma"),
                rs.getString("turma"));
    }

    private ProfessorResponse mapProfessor(ResultSet rs, int rowNum) throws SQLException {
        return new ProfessorResponse(
                (Integer) rs.getObject("codigo"),
                rs.getString("nome"),
                rs.getString("cpf"));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
