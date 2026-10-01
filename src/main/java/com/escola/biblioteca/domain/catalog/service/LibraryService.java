package com.escola.biblioteca.domain.catalog.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Library;
import com.escola.biblioteca.domain.catalog.repository.LibraryRepository;
import com.escola.biblioteca.dto.request.LibraryRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<Library> findByInstitutionId(UUID institutionId) {
        return libraryRepository.findByInstitutionId(institutionId);
    }

    public Library findById(UUID id) {
        return libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.library"));
    }

    @Transactional
    public Library create(UUID institutionId, LibraryRequest request) {
        Library library = new Library();
        library.setInstitutionId(institutionId);
        library.setName(request.name());
        library.setIsCentral(request.isCentral());
        library.marcarNovo();

        Library saved = libraryRepository.save(library);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Library update(UUID id, LibraryRequest request) {
        Library library = findById(id);
        library.setName(request.name());
        library.setIsCentral(request.isCentral());

        Library saved = libraryRepository.save(library);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        libraryRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
    }
}
