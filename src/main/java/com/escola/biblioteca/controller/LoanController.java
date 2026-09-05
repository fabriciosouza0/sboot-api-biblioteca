package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import com.escola.biblioteca.domain.repository.LoanRepository;
import com.escola.biblioteca.domain.service.LoanService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanRepository loanRepository;

    public LoanController(LoanService loanService, LoanRepository loanRepository) {
        this.loanService = loanService;
        this.loanRepository = loanRepository;
    }

    @GetMapping
    public List<Loan> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) String term,
                             @RequestParam(required = false) LoanStatus status) {
        var all = new java.util.ArrayList<Loan>();
        loanRepository.findAll().forEach(all::add);
        if (status != null) {
            all.removeIf(l -> l.getStatus() != status);
        }
        return all;
    }

    @PostMapping
    public ResponseEntity<Loan> checkout(@PathVariable UUID institutionId, @RequestBody LoanRequest request) {
        Loan loan = loanService.checkout(request.patronId(), request.itemId(), request.libraryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Loan> devolver(@PathVariable UUID institutionId, @PathVariable UUID id,
                                         @RequestParam(defaultValue = "OK") String condition) {
        Loan loan = loanService.returnLoan(id, null, condition);
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/{id}/renew")
    public Loan renovar(@PathVariable UUID institutionId, @PathVariable UUID id) {
        return loanService.renew(id);
    }

    public record LoanRequest(UUID patronId, UUID itemId, UUID libraryId) {}
}
