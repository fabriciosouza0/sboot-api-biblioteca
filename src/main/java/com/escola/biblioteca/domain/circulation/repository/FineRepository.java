package com.escola.biblioteca.domain.circulation.repository;

import com.escola.biblioteca.domain.circulation.model.Fine;
import com.escola.biblioteca.domain.circulation.model.enums.FineStatus;
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

    @Query("SELECT COUNT(*) FROM fine f JOIN patron p ON f.patron_id = p.id WHERE p.institution_id = :institutionId AND f.status IN ('PENDING','PARTIAL')")
    long countPendingByInstitutionId(UUID institutionId);

    @Query("SELECT COALESCE(SUM(f.balance_cents), 0) FROM fine f JOIN patron p ON f.patron_id = p.id WHERE p.institution_id = :institutionId AND f.status IN ('PENDING','PARTIAL')")
    long sumBalanceByInstitutionId(UUID institutionId);

    @Query("SELECT COUNT(*) FROM fine WHERE status IN ('PENDING','PARTIAL')")
    long countAllPending();

    @Query("SELECT COALESCE(SUM(balance_cents), 0) FROM fine WHERE status IN ('PENDING','PARTIAL')")
    long sumAllBalance();
}
