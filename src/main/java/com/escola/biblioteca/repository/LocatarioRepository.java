package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Locatario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocatarioRepository extends JpaRepository<Locatario, String> {

    Optional<Locatario> findByAlunoId(Integer idAluno);

    Optional<Locatario> findByProfessorId(Integer idProfessor);

    @EntityGraph(attributePaths = {"aluno", "aluno.turma"})
    List<Locatario> findByAlunoIsNotNullOrderByNomeAsc();

    @EntityGraph(attributePaths = {"professor"})
    List<Locatario> findByProfessorIsNotNullOrderByNomeAsc();

    @Query("SELECT l FROM Locatario l JOIN FETCH l.aluno a JOIN FETCH a.turma WHERE UPPER(l.nome) LIKE CONCAT('%', UPPER(:nome), '%') ORDER BY l.nome")
    List<Locatario> searchAlunosPorNome(@Param("nome") String nome);

    @Query("SELECT l FROM Locatario l JOIN FETCH l.professor WHERE UPPER(l.nome) LIKE CONCAT('%', UPPER(:nome), '%') ORDER BY l.nome")
    List<Locatario> searchProfessoresPorNome(@Param("nome") String nome);
}
