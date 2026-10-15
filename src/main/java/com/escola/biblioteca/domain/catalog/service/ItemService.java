package com.escola.biblioteca.domain.catalog.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import com.escola.biblioteca.domain.catalog.repository.ItemQueryRepository;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.dto.request.ItemRequest;
import com.escola.biblioteca.dto.response.ItemResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemQueryRepository itemQueryRepository;
    private final DashboardPublisher dashboardPublisher;

    public Page<ItemResponse> findAllWithDetails(UUID workId, UUID libraryId, ItemStatus status, String term, Pageable pageable) {
        return itemQueryRepository.findAllWithDetails(workId, libraryId, status, term, pageable);
    }

    public List<ItemResponse> findAllWithDetails(UUID workId, UUID libraryId, ItemStatus status, String term) {
        return itemQueryRepository.findAllWithDetails(workId, libraryId, status, term);
    }

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
    public Item transfer(UUID id, UUID targetLibraryId, String transferNote) {
        Item item = findById(id);
        if (targetLibraryId == null || targetLibraryId.equals(item.getLibraryId())) {
            throw new BusinessException("A filial de destino deve ser diferente da filial de origem.");
        }
        item.setTargetLibraryId(targetLibraryId);
        item.setStatus(ItemStatus.IN_TRANSIT);
        item.setTransferNote(transferNote);

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    public Item transfer(UUID id, UUID targetLibraryId) {
        return transfer(id, targetLibraryId, null);
    }

    @Transactional
    public Item receive(UUID id, UUID activeLibraryId, boolean isGlobalAdmin) {
        Item item = findById(id);
        if (item.getStatus() != ItemStatus.IN_TRANSIT) {
            throw new BusinessException("Apenas exemplares em trânsito podem ser recebidos.");
        }

        UUID destination = item.getTargetLibraryId() != null ? item.getTargetLibraryId() : item.getLibraryId();

        if (!isGlobalAdmin) {
            if (activeLibraryId == null) {
                throw new BusinessException("Filial ativa não informada para confirmar o recebimento.");
            }
            if (!activeLibraryId.equals(destination)) {
                throw new BusinessException("Apenas operadores da filial de destino podem confirmar o recebimento.");
            }
        }

        item.setLibraryId(destination);
        item.setTargetLibraryId(null);
        item.setStatus(ItemStatus.AVAILABLE);
        item.setTransferNote(null);

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    public Item receive(UUID id) {
        return receive(id, null, true);
    }

    @Transactional
    public Item withdraw(UUID id, String reason) {
        Item item = findById(id);
        item.setStatus(ItemStatus.WITHDRAWN);
        item.setWithdrawReason(reason);

        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    public Item withdraw(UUID id) {
        return withdraw(id, null);
    }
}
