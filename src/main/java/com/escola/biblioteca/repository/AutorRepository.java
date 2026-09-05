package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Autor;
import org.springframework.data.repository.CrudRepository;

public interface AutorRepository extends CrudRepository<Autor, Integer> {
}