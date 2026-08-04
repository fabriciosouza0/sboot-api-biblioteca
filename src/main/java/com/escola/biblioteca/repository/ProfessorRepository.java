package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Integer> {
}
