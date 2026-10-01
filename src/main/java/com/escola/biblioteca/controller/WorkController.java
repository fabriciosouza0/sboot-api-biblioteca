package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.catalog.model.Work;
import com.escola.biblioteca.domain.catalog.service.WorkService;
import com.escola.biblioteca.dto.request.WorkRequest;
import com.escola.biblioteca.dto.response.WorkDependenciesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @GetMapping
    public List<Work> listar(@RequestHeader(value = "X-Institution-Id", required = false) UUID institutionId,
                             @RequestParam(required = false) String term) {
        return workService.search(institutionId, term);
    }

    @PostMapping
    public ResponseEntity<Work> criar(@RequestHeader(value = "X-Institution-Id", required = false) UUID institutionId,
                                      @RequestBody WorkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workService.create(institutionId, request));
    }

    @PutMapping("/{id}")
    public Work atualizar(@PathVariable UUID id, @RequestBody WorkRequest request) {
        return workService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        boolean deleted = workService.delete(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/dependencies")
    public WorkDependenciesResponse dependencies(@PathVariable UUID id) {
        return workService.getDependencies(id);
    }

    @PostMapping("/enrich/{isbn13}")
    public ResponseEntity<Void> enrichFromIsbn(@PathVariable String isbn13) {
        // TODO: Implement ISBN enrichment via Google Books/OpenLibrary
        return ResponseEntity.ok().build();
    }
}
