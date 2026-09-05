package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Library;
import com.escola.biblioteca.domain.repository.LibraryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/libraries")
public class LibraryController {

    private final LibraryRepository libraryRepository;

    public LibraryController(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    @GetMapping
    public List<Library> listar(@PathVariable UUID institutionId,
                                @RequestParam(required = false) String term) {
        List<Library> libraries = libraryRepository.findByInstitutionId(institutionId);
        if (term != null && !term.isBlank()) {
            return libraries.stream()
                    .filter(l -> l.getName().toLowerCase().contains(term.toLowerCase()))
                    .toList();
        }
        return libraries;
    }

    @PostMapping
    public ResponseEntity<Library> criar(@PathVariable UUID institutionId, @RequestBody LibraryRequest request) {
        Library library = new Library();
        library.setInstitutionId(institutionId);
        library.setName(request.name());
        library.setIsCentral(request.isCentral());
        library.marcarNovo();
        Library saved = libraryRepository.save(library);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Library atualizar(@PathVariable UUID institutionId, @PathVariable UUID id,
                             @RequestBody LibraryRequest request) {
        Library library = libraryRepository.findById(id).orElseThrow(() ->
                new com.escola.biblioteca.exception.ResourceNotFoundException("error.notfound.library"));
        library.setName(request.name());
        library.setIsCentral(request.isCentral());
        return libraryRepository.save(library);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        libraryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record LibraryRequest(String name, Boolean isCentral) {}
}
