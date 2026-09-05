package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Fine;
import com.escola.biblioteca.domain.model.FineStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FineRepository extends CrudRepository<Fine, UUID> {
    List<Fine> findByPatronIdAndStatusIn(UUID patronId, List<FineStatus> statuses);
    List<Fine> findPendingByPatronId(UUID patronId);

    @Query("SELECT COALESCE(SUM(balance_cents), 0) FROM fine WHERE patron_id = :patronId AND status IN ('PENDING','PARTIAL')")
    Integer sumBalanceByPatronId(UUID patronId);
}