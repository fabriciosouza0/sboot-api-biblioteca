package com.escola.biblioteca.domain.catalog.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.dto.request.ItemRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<Item> findByWorkId(UUID workId) {
        return itemRepository.findByWorkId(workId);
    }

    public List<Item> findAll() {
        var all = new ArrayList<Item>();
        itemRepository.findAll().forEach(all::add);
        return all;
    }

    public Item findById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
    }

    @Transactional
    public Item create(ItemRequest request) {
        Item item = new Item();
        item.setWorkId(request.workId());
        item.setLibraryId(request.libraryId());
        item.setBarcode(request.barcode());
        item.setCallNumber(request.callNumber());
        item.setStatus(ItemStatus.AVAILABLE);
        item.marcarNovo();

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Item update(UUID id, ItemRequest request) {
        Item item = findById(id);
        item.setWorkId(request.workId());
        item.setLibraryId(request.libraryId());
        item.setBarcode(request.barcode());
        item.setCallNumber(request.callNumber());

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        itemRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
    }

    @Transactional
    public Item transfer(UUID id, UUID targetLibraryId) {
        Item item = findById(id);
        item.setLibraryId(targetLibraryId);
        item.setStatus(ItemStatus.IN_TRANSIT);

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Item receive(UUID id) {
        Item item = findById(id);
        item.setStatus(ItemStatus.AVAILABLE);

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Item withdraw(UUID id) {
        Item item = findById(id);
        item.setStatus(ItemStatus.WITHDRAWN);

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }
}
