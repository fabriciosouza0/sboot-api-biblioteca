package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Library;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryRepository extends CrudRepository<Library, UUID> {
    List<Library> findByInstitutionId(UUID institutionId);
    Optional<Library> findByInstitutionIdAndIsCentralTrue(UUID institutionId);

    @Query("SELECT COUNT(*) FROM library WHERE institution_id = :institutionId")
    long countByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(*) FROM library")
    long countAll();
}
