package com.escola.biblioteca.domain.circulation.repository;

import com.escola.biblioteca.domain.circulation.model.Hold;
import com.escola.biblioteca.domain.circulation.model.enums.HoldStatus;
import com.escola.biblioteca.dto.response.WorkDependenciesResponse;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldRepository extends CrudRepository<Hold, UUID> {
    List<Hold> findByPatronId(UUID patronId);
    List<Hold> findWaitingByWorkIdAndLibraryId(UUID workId, UUID libraryId);
    Optional<Hold> findNextWaitingByWorkIdAndLibraryId(UUID workId, UUID libraryId);
    List<Hold> findByWorkIdAndStatus(UUID workId, HoldStatus status);

    @Query("SELECT COUNT(*) FROM hold h JOIN work w ON h.work_id = w.id WHERE w.institution_id = :institutionId AND h.status = :status")
    long countByInstitutionIdAndStatus(UUID institutionId, String status);

    @Query("SELECT COUNT(*) FROM hold h JOIN work w ON h.work_id = w.id WHERE h.status = :status")
    long countAllByStatus(String status);

    @Query("SELECT h.id, p.name, l.name, h.status, h.position " +
            "FROM hold h JOIN patron p ON h.patron_id = p.id JOIN library l ON h.library_id = l.id WHERE h.work_id = :workId")
    List<WorkDependenciesResponse.HoldSummary> findDependencyHolds(UUID workId);
}
