package com.escola.biblioteca.domain.patron.repository;

import com.escola.biblioteca.domain.patron.model.Patron;
import com.escola.biblioteca.domain.patron.model.enums.PatronStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatronRepository extends CrudRepository<Patron, UUID> {
    Optional<Patron> findByInstitutionIdAndExternalId(UUID institutionId, String externalId);
    List<Patron> findByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(*) FROM patron WHERE institution_id = :institutionId")
    long countByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(*) FROM patron WHERE institution_id = :institutionId AND status = :status")
    long countByInstitutionIdAndStatus(UUID institutionId, String status);

    @Query("SELECT COUNT(*) FROM patron")
    long countAll();

    @Query("SELECT COUNT(*) FROM patron WHERE status = :status")
    long countAllByStatus(String status);
}
