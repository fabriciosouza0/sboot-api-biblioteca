package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Patron;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatronRepository extends CrudRepository<Patron, UUID> {
    Optional<Patron> findByInstitutionIdAndExternalId(UUID institutionId, String externalId);
    List<Patron> findByInstitutionId(UUID institutionId);
}