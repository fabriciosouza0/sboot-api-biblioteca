package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Loca;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocaRepository extends JpaRepository<Loca, Integer> {

    @EntityGraph(attributePaths = {"livro", "livro.autor", "livro.cdd", "locatario"})
    List<Loca> findAllByOrderByDataDeLocacaoDesc();

    @EntityGraph(attributePaths = {"livro", "locatario", "locatario.professor", "locatario.aluno"})
    @Override
    List<Loca> findAll();

    @Query("SELECT l FROM Loca l JOIN FETCH l.livro JOIN FETCH l.livro.autor JOIN FETCH l.livro.cdd JOIN FETCH l.locatario WHERE UPPER(l.locatario.nome) LIKE CONCAT('%', UPPER(:nome), '%') ORDER BY l.dataDeLocacao DESC")
    List<Loca> searchPorLocatario(@Param("nome") String nome);
}
