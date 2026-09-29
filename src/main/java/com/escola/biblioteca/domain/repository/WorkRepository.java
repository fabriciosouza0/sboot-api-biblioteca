package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Work;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkRepository extends CrudRepository<Work, UUID> {
    List<Work> findByInstitutionId(UUID institutionId);
    Optional<Work> findByInstitutionIdAndIsbn13(UUID institutionId, String isbn13);

    @Query("SELECT COUNT(*) FROM work WHERE institution_id = :institutionId")
    long countByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(*) FROM work")
    long countAll();
}
