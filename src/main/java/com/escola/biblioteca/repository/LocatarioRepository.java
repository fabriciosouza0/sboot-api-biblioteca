package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Locatario;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface LocatarioRepository extends CrudRepository<Locatario, String> {

    Optional<Locatario> findByCodigoAluno(Integer codigoAluno);

    Optional<Locatario> findByCodigoProfessor(Integer codigoProfessor);
}