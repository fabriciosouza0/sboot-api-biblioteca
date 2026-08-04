package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Adm;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmRepository extends JpaRepository<Adm, Integer> {

    Optional<Adm> findByLogin(String login);
}
