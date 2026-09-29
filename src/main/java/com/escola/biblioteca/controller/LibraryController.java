package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.model.Library;
import com.escola.biblioteca.domain.repository.LibraryRepository;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/libraries")
public class LibraryController {

    private final LibraryRepository libraryRepository;
    private final DashboardPublisher dashboardPublisher;

    public LibraryController(LibraryRepository libraryRepository, DashboardPublisher dashboardPublisher) {
        this.libraryRepository = libraryRepository;
        this.dashboardPublisher = dashboardPublisher;
    }

    @GetMapping
    public List<Library> listar(@PathVariable UUID institutionId) {
        return libraryRepository.findByInstitutionId(institutionId);
    }

    @PostMapping
    public ResponseEntity<Library> criar(@PathVariable UUID institutionId, @RequestBody LibraryRequest request) {
        Library library = new Library();
        library.setInstitutionId(institutionId);
        library.setName(request.name());
        library.setIsCentral(request.isCentral());
        library.marcarNovo();
        Library saved = libraryRepository.save(library);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Library atualizar(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody LibraryRequest request) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.library"));
        library.setName(request.name());
        library.setIsCentral(request.isCentral());
        Library saved = libraryRepository.save(library);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        libraryRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.noContent().build();
    }

    public record LibraryRequest(String name, boolean isCentral) {}
}
