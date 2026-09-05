package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Aluno;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface AlunoRepository extends CrudRepository<Aluno, Integer> {

    Optional<Aluno> findByCodigoTurma(Integer codigoTurma);

}
