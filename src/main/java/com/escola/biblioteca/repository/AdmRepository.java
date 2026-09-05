package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Adm;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface AdmRepository extends CrudRepository<Adm, Integer> {

    Optional<Adm> findByLogin(String login);
}