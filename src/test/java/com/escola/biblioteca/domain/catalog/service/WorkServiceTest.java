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
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private WorkQueryRepository workQueryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private HoldRepository holdRepository;

    @Mock
    private DashboardPublisher dashboardPublisher;

    @InjectMocks
    private WorkService workService;

    private UUID workId;
    private UUID institutionId;
    private Work work;

    @BeforeEach
    void setUp() {
        workId = UUID.randomUUID();
        institutionId = UUID.randomUUID();
        work = new Work();
        work.setId(workId);
        work.setInstitutionId(institutionId);
        work.setTitle("Dom Casmurro");
    }

    @Test
    void search_delegatesToQueryRepository() {
        when(workQueryRepository.searchByTitle(institutionId, "Dom")).thenReturn(List.of(work));

        List<Work> result = workService.search(institutionId, "Dom");

        assertEquals(1, result.size());
        assertEquals("Dom Casmurro", result.get(0).getTitle());
        verify(workQueryRepository).searchByTitle(institutionId, "Dom");
    }

    @Test
    void findById_found_returnsWork() {
        when(workRepository.findById(workId)).thenReturn(Optional.of(work));

        Work found = workService.findById(workId);

        assertNotNull(found);
        assertEquals(workId, found.getId());
    }

    @Test
    void findById_notFound_throwsException() {
        when(workRepository.findById(workId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> workService.findById(workId));
    }

    @Test
    void create_savesWorkAndNotifiesDashboard() {
        WorkRequest request = new WorkRequest("9781234567890", "Novo Livro", "Autor", "Editora", 2024, "1", "82", "url", "desc");
        when(workRepository.save(any(Work.class))).thenAnswer(inv -> inv.getArgument(0));

        Work created = workService.create(institutionId, request);

        assertNotNull(created);
        assertEquals("Novo Livro", created.getTitle());
        assertEquals(institutionId, created.getInstitutionId());
        verify(workRepository).save(any(Work.class));
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void update_modifiesWorkAndNotifiesDashboard() {
        when(workRepository.findById(workId)).thenReturn(Optional.of(work));
        when(workRepository.save(any(Work.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkRequest request = new WorkRequest("9781234567890", "Titulo Atualizado", "Autor", "Editora", 2024, "1", "82", "url", "desc");
        Work updated = workService.update(workId, request);

        assertEquals("Titulo Atualizado", updated.getTitle());
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void delete_withoutDependencies_deletesAndNotifiesDashboard() {
        when(itemRepository.findDependencyItems(workId)).thenReturn(List.of());
        when(holdRepository.findDependencyHolds(workId)).thenReturn(List.of());

        boolean deleted = workService.delete(workId);

        assertTrue(deleted);
        verify(workRepository).deleteById(workId);
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void delete_withDependencies_returnsFalseWithoutDeleting() {
        var itemSummary = new WorkDependenciesResponse.ItemSummary(UUID.randomUUID().toString(), "BARCODE-01", "82-3", "AVAILABLE", "Bib Central");
        when(itemRepository.findDependencyItems(workId)).thenReturn(List.of(itemSummary));

        boolean deleted = workService.delete(workId);

        assertFalse(deleted);
        verify(workRepository, never()).deleteById(any());
        verify(dashboardPublisher, never()).dadosAlterados();
    }

    @Test
    void getDependencies_returnsItemsAndHolds() {
        when(itemRepository.findDependencyItems(workId)).thenReturn(List.of());
        when(holdRepository.findDependencyHolds(workId)).thenReturn(List.of());

        WorkDependenciesResponse deps = workService.getDependencies(workId);

        assertNotNull(deps);
        assertTrue(deps.items().isEmpty());
        assertTrue(deps.holds().isEmpty());
    }
}
