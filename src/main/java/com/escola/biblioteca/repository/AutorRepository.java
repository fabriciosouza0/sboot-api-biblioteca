package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Autor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Integer> {

    List<Autor> findByNomeContainingIgnoreCase(String nome);
}
