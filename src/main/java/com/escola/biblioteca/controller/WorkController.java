package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.model.Work;
import com.escola.biblioteca.domain.repository.HoldRepository;
import com.escola.biblioteca.domain.repository.ItemRepository;
import com.escola.biblioteca.domain.repository.WorkRepository;
import com.escola.biblioteca.domain.repository.query.WorkQueryRepository;
import com.escola.biblioteca.dto.response.WorkDependenciesResponse;
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
    private final ItemRepository itemRepository;
    private final HoldRepository holdRepository;
    private final DashboardPublisher dashboardPublisher;

    public WorkController(WorkRepository workRepository, WorkQueryRepository workQueryRepository,
                          ItemRepository itemRepository, HoldRepository holdRepository,
                          DashboardPublisher dashboardPublisher) {
        this.workRepository = workRepository;
        this.workQueryRepository = workQueryRepository;
        this.itemRepository = itemRepository;
        this.holdRepository = holdRepository;
        this.dashboardPublisher = dashboardPublisher;
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
        Work saved = workRepository.save(work);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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
        Work saved = workRepository.save(work);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        List<WorkDependenciesResponse.ItemSummary> items = itemRepository.findDependencyItems(id);
        List<WorkDependenciesResponse.HoldSummary> holds = holdRepository.findDependencyHolds(id);
        if (!items.isEmpty() || !holds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        workRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/dependencies")
    public WorkDependenciesResponse dependencies(@PathVariable UUID institutionId, @PathVariable UUID id) {
        return new WorkDependenciesResponse(
                itemRepository.findDependencyItems(id),
                holdRepository.findDependencyHolds(id));
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
