package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.circulation.model.Loan;
import com.escola.biblioteca.domain.circulation.service.LoanService;
import com.escola.biblioteca.dto.response.LoanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public Page<LoanResponse> listar(
            @RequestParam(required = false) UUID patronId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String term,
            @PageableDefault(size = 20) Pageable pageable) {
        return loanService.findAllWithDetails(patronId, status, term, pageable);
    }

    @PostMapping
    public ResponseEntity<Loan> criar(@RequestBody LoanRequest request) {
        Loan loan = loanService.checkout(request.patronId(), request.itemId(), request.libraryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Loan> devolver(@PathVariable UUID loanId,
                                         @RequestParam(required = false) UUID returnLibraryId,
                                         @RequestParam(defaultValue = "OK") String condition) {
        Loan returned = loanService.returnLoan(loanId, returnLibraryId, condition);
        return ResponseEntity.ok(returned);
    }

    @PostMapping("/{loanId}/renew")
    public Loan renovar(@PathVariable UUID loanId) {
        return loanService.renew(loanId);
    }

    public record LoanRequest(UUID patronId, UUID itemId, UUID libraryId) {}
}
