package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Item;
import com.escola.biblioteca.domain.model.ItemStatus;
import com.escola.biblioteca.domain.model.OutboxEvent;
import com.escola.biblioteca.domain.model.Work;
import com.escola.biblioteca.domain.repository.ItemRepository;
import com.escola.biblioteca.domain.repository.WorkRepository;
import com.escola.biblioteca.domain.repository.LibraryRepository;
import com.escola.biblioteca.domain.repository.OutboxEventRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final WorkRepository workRepository;
    private final ItemRepository itemRepository;
    private final LibraryRepository libraryRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public Work registerWork(UUID institutionId, String isbn13, String title, String authorsJson,
                             String publisher, Integer publishedYear, String edition,
                             String cdu, String coverUrl, String description) {
        if (isbn13 != null && workRepository.findByInstitutionIdAndIsbn13(institutionId, isbn13).isPresent()) {
            throw new BusinessException("error.work.isbn.duplicate", isbn13);
        }
        Work work = new Work();
        work.setInstitutionId(institutionId);
        work.setIsbn13(isbn13);
        work.setTitle(title);
        work.setAuthors(authorsJson != null ? authorsJson : "[]");
        work.setPublisher(publisher);
        work.setPublishedYear(publishedYear);
        work.setEdition(edition);
        work.setCdu(cdu);
        work.setCoverUrl(coverUrl);
        work.setDescription(description);
        work.marcarNovo();
        Work saved = workRepository.save(work);
        publishEvent(saved.getId(), "WorkRegistered", saved);
        return saved;
    }

    @Transactional
    public Work enrichFromIsbn(UUID institutionId, String isbn13) {
        // TODO: Implement external API call (Google Books / Open Library)
        // For now, just find existing or create placeholder
        return workRepository.findByInstitutionIdAndIsbn13(institutionId, isbn13)
                .orElseGet(() -> registerWork(institutionId, isbn13, "Título a confirmar", "[]", null, null, null, null, null, null));
    }

    @Transactional
    public Item registerItem(UUID workId, UUID libraryId, String barcode, String callNumber) {
        if (itemRepository.findByBarcode(barcode).isPresent()) {
            throw new BusinessException("error.item.barcode.duplicate", barcode);
        }
        Item item = new Item();
        item.setWorkId(workId);
        item.setLibraryId(libraryId);
        item.setBarcode(barcode);
        item.setCallNumber(callNumber);
        item.setStatus(ItemStatus.AVAILABLE);
        item.marcarNovo();
        Item saved = itemRepository.save(item);
        publishEvent(saved.getId(), "ItemRegistered", saved);
        return saved;
    }

    @Transactional
    public Item transferItem(UUID itemId, UUID toLibraryId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));

        if (item.getStatus() == ItemStatus.ON_LOAN || item.getStatus() == ItemStatus.IN_TRANSIT) {
            throw new BusinessException("error.item.cannot.transfer", item.getStatus());
        }

        libraryRepository.findById(toLibraryId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.library"));

        UUID fromLibraryId = item.getLibraryId();
        item.setLibraryId(toLibraryId);
        item.setStatus(ItemStatus.IN_TRANSIT);
        Item saved = itemRepository.save(item);
        publishEvent(saved.getId(), "ItemTransferred", saved);
        return saved;
    }

    @Transactional
    public Item receiveTransfer(UUID itemId, UUID expectedLibraryId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));

        if (item.getStatus() != ItemStatus.IN_TRANSIT) {
            throw new BusinessException("error.item.not.in.transit");
        }
        if (!item.getLibraryId().equals(expectedLibraryId)) {
            throw new BusinessException("error.item.transfer.mismatch");
        }

        item.setStatus(ItemStatus.AVAILABLE);
        Item saved = itemRepository.save(item);
        publishEvent(saved.getId(), "ItemReceived", saved);
        return saved;
    }

    @Transactional
    public Item withdrawItem(UUID itemId, String reason) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));

        if (item.getStatus() == ItemStatus.ON_LOAN) {
            throw new BusinessException("error.item.withdraw.on.loan");
        }

        item.setStatus(ItemStatus.WITHDRAWN);
        Item saved = itemRepository.save(item);
        publishEvent(saved.getId(), "ItemWithdrawn", saved);
        return saved;
    }

    public List<Work> searchByTitle(UUID institutionId, String term) {
        return workRepository.findByInstitutionId(institutionId); // Query repo would be used in controller
    }

    public List<Item> findAvailableByWorkAndLibrary(UUID workId, UUID libraryId) {
        return itemRepository.findByWorkIdAndStatus(workId, ItemStatus.AVAILABLE);
    }

    private void publishEvent(UUID aggregateId, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Item");
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(com.escola.biblioteca.util.JsonUtil.toJson(payload));
        event.marcarNovo();
        outboxEventRepository.save(event);
    }
}