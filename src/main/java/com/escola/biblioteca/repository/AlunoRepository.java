package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Aluno;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Integer> {

    Optional<Aluno> findByTurmaId(Integer idTurma);
}
