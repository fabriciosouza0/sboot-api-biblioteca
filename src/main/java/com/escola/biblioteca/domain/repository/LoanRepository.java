package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends CrudRepository<Loan, UUID> {
    List<Loan> findByPatronIdAndStatusIn(UUID patronId, List<LoanStatus> statuses);
    List<Loan> findOverdueByPatronId(UUID patronId);
}