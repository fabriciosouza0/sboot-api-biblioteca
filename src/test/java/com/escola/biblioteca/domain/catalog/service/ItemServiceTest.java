package com.escola.biblioteca.domain.catalog.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.dto.request.ItemRequest;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private DashboardPublisher dashboardPublisher;

    @InjectMocks
    private ItemService itemService;

    private UUID itemId;
    private UUID workId;
    private UUID libraryId;
    private Item item;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        workId = UUID.randomUUID();
        libraryId = UUID.randomUUID();

        item = new Item();
        item.setId(itemId);
        item.setWorkId(workId);
        item.setLibraryId(libraryId);
        item.setBarcode("BC-12345");
        item.setCallNumber("82-3 CAS");
        item.setStatus(ItemStatus.AVAILABLE);
    }

    @Test
    void findByWorkId_returnsList() {
        when(itemRepository.findByWorkId(workId)).thenReturn(List.of(item));

        List<Item> result = itemService.findByWorkId(workId);

        assertEquals(1, result.size());
        assertEquals("BC-12345", result.get(0).getBarcode());
        verify(itemRepository).findByWorkId(workId);
    }

    @Test
    void findAll_returnsList() {
        when(itemRepository.findAll()).thenReturn(List.of(item));

        List<Item> result = itemService.findAll();

        assertEquals(1, result.size());
        assertEquals(itemId, result.get(0).getId());
        verify(itemRepository).findAll();
    }

    @Test
    void findById_found_returnsItem() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Item found = itemService.findById(itemId);

        assertNotNull(found);
        assertEquals(itemId, found.getId());
    }

    @Test
    void findById_notFound_throwsException() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.findById(itemId));
    }

    @Test
    void create_savesItemAndNotifiesDashboard() {
        ItemRequest request = new ItemRequest(workId, libraryId, "BC-NEW", "CALL-NEW");
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item created = itemService.create(request);

        assertNotNull(created);
        assertEquals("BC-NEW", created.getBarcode());
        assertEquals(ItemStatus.AVAILABLE, created.getStatus());
        verify(itemRepository).save(any(Item.class));
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void update_modifiesItemAndNotifiesDashboard() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemRequest request = new ItemRequest(workId, libraryId, "BC-UPDATED", "CALL-UPDATED");
        Item updated = itemService.update(itemId, request);

        assertEquals("BC-UPDATED", updated.getBarcode());
        assertEquals("CALL-UPDATED", updated.getCallNumber());
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void delete_deletesItemAndNotifiesDashboard() {
        itemService.delete(itemId);

        verify(itemRepository).deleteById(itemId);
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void transfer_setsInTransitAndNotifiesDashboard() {
        UUID targetLib = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item transferred = itemService.transfer(itemId, targetLib);

        assertEquals(targetLib, transferred.getLibraryId());
        assertEquals(ItemStatus.IN_TRANSIT, transferred.getStatus());
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void receive_setsAvailableAndNotifiesDashboard() {
        item.setStatus(ItemStatus.IN_TRANSIT);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item received = itemService.receive(itemId);

        assertEquals(ItemStatus.AVAILABLE, received.getStatus());
        verify(dashboardPublisher).dadosAlterados();
    }

    @Test
    void withdraw_setsWithdrawnAndNotifiesDashboard() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item withdrawn = itemService.withdraw(itemId);

        assertEquals(ItemStatus.WITHDRAWN, withdrawn.getStatus());
        verify(dashboardPublisher).dadosAlterados();
    }
}
