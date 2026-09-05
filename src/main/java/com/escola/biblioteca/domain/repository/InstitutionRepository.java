package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Institution;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends CrudRepository<Institution, UUID> {
    Optional<Institution> findByCode(String code);
}