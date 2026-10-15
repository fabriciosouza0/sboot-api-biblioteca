package com.escola.biblioteca.domain.circulation.repository;

import com.escola.biblioteca.domain.circulation.model.Loan;
import com.escola.biblioteca.domain.circulation.model.enums.LoanStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends CrudRepository<Loan, UUID> {
    List<Loan> findByPatronIdAndStatusIn(UUID patronId, List<LoanStatus> statuses);
    List<Loan> findOverdueByPatronId(UUID patronId);

    @Query("SELECT * FROM loan WHERE status = 'ACTIVE' AND due_at < :now")
    List<Loan> findActiveLoansDueBefore(OffsetDateTime now);

    @Query("SELECT * FROM loan WHERE status = 'ACTIVE' AND due_at >= :start AND due_at <= :end")
    List<Loan> findActiveLoansDueBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT * FROM loan WHERE status = 'OVERDUE'")
    List<Loan> findAllOverdueLoans();

    @Query("SELECT COUNT(*) FROM loan lo JOIN item i ON lo.item_id = i.id JOIN library l ON i.library_id = l.id WHERE l.institution_id = :institutionId AND lo.status = :status")
    long countByInstitutionIdAndStatus(UUID institutionId, String status);

    @Query("SELECT COUNT(*) FROM loan lo JOIN item i ON lo.item_id = i.id JOIN library l ON i.library_id = l.id WHERE lo.status = :status")
    long countAllByStatus(String status);
}
