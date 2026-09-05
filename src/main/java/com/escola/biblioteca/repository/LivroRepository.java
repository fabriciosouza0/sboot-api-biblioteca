package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Livro;
import org.springframework.data.repository.CrudRepository;

public interface LivroRepository extends CrudRepository<Livro, Long> {
}