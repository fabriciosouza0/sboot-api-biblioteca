package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import com.escola.biblioteca.domain.repository.LoanRepository;
import com.escola.biblioteca.domain.service.LoanService;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/loans")
public class LoanController {

    private final LoanRepository loanRepository;
    private final LoanService loanService;

    public LoanController(LoanRepository loanRepository, LoanService loanService) {
        this.loanRepository = loanRepository;
        this.loanService = loanService;
    }

    @GetMapping
    public List<Loan> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) UUID patronId) {
        if (patronId != null) {
            return loanService.findActiveByPatron(patronId);
        }
        var all = new java.util.ArrayList<Loan>();
        loanRepository.findAll().forEach(all::add);
        return all;
    }

    @PostMapping
    public ResponseEntity<Loan> criar(@PathVariable UUID institutionId, @RequestBody LoanRequest request) {
        Loan loan = loanService.checkout(request.patronId(), request.itemId(), request.libraryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Loan> devolver(@PathVariable UUID institutionId, @PathVariable UUID loanId,
                                         @RequestParam(defaultValue = "OK") String condition) {
        // Find the library from the loan itself
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.loan"));
        Loan returned = loanService.returnLoan(loanId, loan.getLibraryId(), condition);
        return ResponseEntity.ok(returned);
    }

    @PostMapping("/{loanId}/renew")
    public Loan renovar(@PathVariable UUID institutionId, @PathVariable UUID loanId) {
        return loanService.renew(loanId);
    }

    public record LoanRequest(UUID patronId, UUID itemId, UUID libraryId) {}
}
