package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Livro;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @EntityGraph(attributePaths = {"autor", "cdd"})
    List<Livro> findByTituloContainingIgnoreCase(String titulo);

    @EntityGraph(attributePaths = {"autor", "cdd"})
    List<Livro> findAllByOrderByTituloAsc();
}
