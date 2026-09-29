package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends CrudRepository<Loan, UUID> {
    List<Loan> findByPatronIdAndStatusIn(UUID patronId, List<LoanStatus> statuses);
    List<Loan> findOverdueByPatronId(UUID patronId);

    @Query("SELECT COUNT(*) FROM loan lo JOIN item i ON lo.item_id = i.id JOIN library l ON i.library_id = l.id WHERE l.institution_id = :institutionId AND lo.status = :status")
    long countByInstitutionIdAndStatus(UUID institutionId, String status);

    @Query("SELECT COUNT(*) FROM loan lo JOIN item i ON lo.item_id = i.id JOIN library l ON i.library_id = l.id WHERE lo.status = :status")
    long countAllByStatus(String status);
}
