package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Work;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkRepository extends CrudRepository<Work, UUID> {
    List<Work> findByInstitutionId(UUID institutionId);
    Optional<Work> findByInstitutionIdAndIsbn13(UUID institutionId, String isbn13);
}