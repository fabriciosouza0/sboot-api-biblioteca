package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Work;
import com.escola.biblioteca.domain.repository.WorkRepository;
import com.escola.biblioteca.domain.repository.query.WorkQueryRepository;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/works")
public class WorkController {

    private final WorkRepository workRepository;
    private final WorkQueryRepository workQueryRepository;

    public WorkController(WorkRepository workRepository, WorkQueryRepository workQueryRepository) {
        this.workRepository = workRepository;
        this.workQueryRepository = workQueryRepository;
    }

    @GetMapping
    public List<Work> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) String term) {
        return workQueryRepository.searchByTitle(institutionId, term);
    }

    @PostMapping
    public ResponseEntity<Work> criar(@PathVariable UUID institutionId, @RequestBody WorkRequest request) {
        Work work = new Work();
        work.setInstitutionId(institutionId);
        work.setIsbn13(request.isbn13());
        work.setTitle(request.title());
        work.setAuthors(request.authors() != null ? request.authors() : "[]");
        work.setPublisher(request.publisher());
        work.setPublishedYear(request.publishedYear());
        work.setEdition(request.edition());
        work.setCdu(request.cdu());
        work.setCoverUrl(request.coverUrl());
        work.setDescription(request.description());
        work.marcarNovo();
        return ResponseEntity.status(HttpStatus.CREATED).body(workRepository.save(work));
    }

    @PutMapping("/{id}")
    public Work atualizar(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody WorkRequest request) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.work"));
        work.setIsbn13(request.isbn13());
        work.setTitle(request.title());
        work.setAuthors(request.authors() != null ? request.authors() : "[]");
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
    public ResponseEntity<Void> enrichFromIsbn(@PathVariable UUID institutionId, @PathVariable String isbn13) {
        // TODO: Implement ISBN enrichment via Google Books/OpenLibrary
        return ResponseEntity.ok().build();
    }

    public record WorkRequest(String isbn13, String title, String authors, String publisher,
                              Integer publishedYear, String edition, String cdu,
                              String coverUrl, String description) {}
}
