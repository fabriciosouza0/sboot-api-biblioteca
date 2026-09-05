package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Professor;
import org.springframework.data.repository.CrudRepository;

public interface ProfessorRepository extends CrudRepository<Professor, Integer> {
}