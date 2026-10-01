package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.patron.model.Patron;
import com.escola.biblioteca.domain.patron.service.PatronService;
import com.escola.biblioteca.dto.request.PatronRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patrons")
@RequiredArgsConstructor
public class PatronController {

    private final PatronService patronService;

    @GetMapping
    public List<Patron> listar(@RequestHeader("X-Institution-Id") UUID institutionId,
                               @RequestParam(required = false) String name) {
        return patronService.search(institutionId, name);
    }

    @PostMapping
    public ResponseEntity<Patron> criar(@RequestHeader("X-Institution-Id") UUID institutionId,
                                        @RequestBody PatronRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patronService.create(institutionId, request));
    }

    @PutMapping("/{id}")
    public Patron atualizar(@PathVariable UUID id, @RequestBody PatronRequest request) {
        return patronService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        patronService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
