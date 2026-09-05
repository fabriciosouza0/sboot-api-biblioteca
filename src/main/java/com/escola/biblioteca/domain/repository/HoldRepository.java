package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Hold;
import com.escola.biblioteca.domain.model.HoldStatus;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldRepository extends CrudRepository<Hold, UUID> {
    List<Hold> findByPatronId(UUID patronId);
    List<Hold> findWaitingByWorkIdAndLibraryId(UUID workId, UUID libraryId);
    Optional<Hold> findNextWaitingByWorkIdAndLibraryId(UUID workId, UUID libraryId);
//    List<Hold> findReadyExpired();
    List<Hold> findByWorkIdAndStatus(UUID workId, HoldStatus status);
}