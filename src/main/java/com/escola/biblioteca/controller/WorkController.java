package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Work;
import com.escola.biblioteca.domain.repository.WorkRepository;
import com.escola.biblioteca.domain.repository.query.WorkQueryRepository;
import com.escola.biblioteca.domain.service.CatalogService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/works")
public class WorkController {

    private final CatalogService catalogService;
    private final WorkRepository workRepository;
    private final WorkQueryRepository workQueryRepository;

    public WorkController(CatalogService catalogService, WorkRepository workRepository,
                          WorkQueryRepository workQueryRepository) {
        this.catalogService = catalogService;
        this.workRepository = workRepository;
        this.workQueryRepository = workQueryRepository;
    }

    @GetMapping
    public List<Work> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) String term) {
        if (term != null && !term.isBlank()) {
            return workQueryRepository.searchByTitle(institutionId, term);
        }
        return workRepository.findByInstitutionId(institutionId);
    }

    @PostMapping
    public ResponseEntity<Work> criar(@PathVariable UUID institutionId, @RequestBody WorkRequest request) {
        Work work = catalogService.registerWork(institutionId, request.isbn13(), request.title(),
                request.authors(), request.publisher(), request.publishedYear(),
                request.edition(), request.cdu(), request.coverUrl(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(work);
    }

    @PutMapping("/{id}")
    public Work atualizar(@PathVariable UUID institutionId, @PathVariable UUID id,
                          @RequestBody WorkRequest request) {
        Work work = workRepository.findById(id).orElseThrow(() ->
                new com.escola.biblioteca.exception.ResourceNotFoundException("error.notfound.work"));
        work.setIsbn13(request.isbn13());
        work.setTitle(request.title());
        work.setAuthors(request.authors());
        work.setPublisher(request.publisher());
        work.setPublishedYear(request.publishedYear());
        work.setEdition(request.edition());
        work.setCdu(request.cdu());
        work.setCoverUrl(request.coverUrl());
        work.setDescription(request.description());
        return workRepository.save(work);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        workRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enrich/{isbn13}")
    public Work enrichFromIsbn(@PathVariable UUID institutionId, @PathVariable String isbn13) {
        return catalogService.enrichFromIsbn(institutionId, isbn13);
    }

    public record WorkRequest(String isbn13, String title, String authors, String publisher,
                              Integer publishedYear, String edition, String cdu,
                              String coverUrl, String description) {}
}
