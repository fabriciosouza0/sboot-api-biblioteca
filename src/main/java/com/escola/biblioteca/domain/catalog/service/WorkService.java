package com.escola.biblioteca.domain.catalog.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Work;
import com.escola.biblioteca.domain.circulation.repository.HoldRepository;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.domain.catalog.repository.WorkRepository;
import com.escola.biblioteca.domain.catalog.repository.WorkQueryRepository;
import com.escola.biblioteca.dto.request.WorkRequest;
import com.escola.biblioteca.dto.response.WorkDependenciesResponse;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final WorkQueryRepository workQueryRepository;
    private final ItemRepository itemRepository;
    private final HoldRepository holdRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<Work> search(UUID institutionId, String term) {
        return workQueryRepository.searchByTitle(institutionId, term);
    }

    public Work findById(UUID id) {
        return workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.work"));
    }

    @Transactional
    public Work create(UUID institutionId, WorkRequest request) {
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
        return saved;
    }

    @Transactional
    public Work update(UUID id, WorkRequest request) {
        Work work = findById(id);
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

    @Transactional
    public boolean delete(UUID id) {
        List<WorkDependenciesResponse.ItemSummary> items = itemRepository.findDependencyItems(id);
        List<WorkDependenciesResponse.HoldSummary> holds = holdRepository.findDependencyHolds(id);
        if (!items.isEmpty() || !holds.isEmpty()) {
            return false;
        }
        workRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
        return true;
    }

    public WorkDependenciesResponse getDependencies(UUID id) {
        return new WorkDependenciesResponse(
                itemRepository.findDependencyItems(id),
                holdRepository.findDependencyHolds(id)
        );
    }
}
