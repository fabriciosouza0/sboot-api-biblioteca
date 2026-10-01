package com.escola.biblioteca.domain.catalog.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Library;
import com.escola.biblioteca.domain.catalog.repository.LibraryRepository;
import com.escola.biblioteca.dto.request.LibraryRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private DashboardPublisher dashboardPublisher;

    @InjectMocks
    private LibraryService libraryService;

    private UUID libraryId;
    private UUID institutionId;
    private Library library;

    @BeforeEach
    void setUp() {
        libraryId = UUID.randomUUID();
        institutionId = UUID.randomUUID();

        library = new Library();
        library.setId(libraryId);
        library.setInstitutionId(institutionId);
        library.setName("Biblioteca Central");
        library.setIsCentral(true);
    }

    @Test
    void findByInstitutionId_returnsList() {
        when(libraryRepository.findByInstitutionId(institutionId)).thenReturn(List.of(library));

        List<Library> result = libraryService.findByInstitutionId(institutionId);

        assertEquals(1, result.size());
        assertEquals("Biblioteca Central", result.get(0).getName());
        verify(libraryRepository).findByInstitutionId(institutionId);
    }

    @Test
    void findById_found_returnsLibrary() {
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));

        Library found = libraryService.findById(libraryId);

        assertNotNull(found);
        assertEquals(libraryId, found.getId());
    }

    @Test
    void findById_notFound_throwsException() {
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> libraryService.findById(libraryId));
    }

    @Test
    void create_savesLibraryAndNotifiesDashboard() {
        LibraryRequest request = new LibraryRequest("Biblioteca Setorial", false);
        when(libraryRepository.save(any(Library.class))).thenAnswer(inv -> inv.getArgument(0));

        Library created = libraryService.create(institutionId, request);

        assertNotNull(created);
        assertEquals("Biblioteca Setorial", created.getName());
        assertEquals(institutionId, created.getInstitutionId());
        assertFalse(created.getIsCentral());
        verify(libraryRepository).save(any(Library.class));
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void update_modifiesLibraryAndNotifiesDashboard() {
        when(libraryRepository.findById(libraryId)).thenReturn(Optional.of(library));
        when(libraryRepository.save(any(Library.class))).thenAnswer(inv -> inv.getArgument(0));

        LibraryRequest request = new LibraryRequest("Biblioteca Central Renomeada", true);
        Library updated = libraryService.update(libraryId, request);

        assertEquals("Biblioteca Central Renomeada", updated.getName());
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void delete_deletesLibraryAndNotifiesDashboard() {
        libraryService.delete(libraryId);

        verify(libraryRepository).deleteById(libraryId);
        verify(dashboardPublisher).dadosAlterados();
    }
}
