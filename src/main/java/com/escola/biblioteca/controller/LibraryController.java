package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.catalog.model.Library;
import com.escola.biblioteca.domain.catalog.service.LibraryService;
import com.escola.biblioteca.dto.request.LibraryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public List<Library> listar(@RequestHeader("X-Institution-Id") UUID institutionId) {
        return libraryService.findByInstitutionId(institutionId);
    }

    @PostMapping
    public ResponseEntity<Library> criar(@RequestHeader("X-Institution-Id") UUID institutionId,
                                         @RequestBody LibraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryService.create(institutionId, request));
    }

    @PutMapping("/{id}")
    public Library atualizar(@PathVariable UUID id, @RequestBody LibraryRequest request) {
        return libraryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        libraryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
