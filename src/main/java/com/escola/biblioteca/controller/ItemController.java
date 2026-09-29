package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.model.Item;
import com.escola.biblioteca.domain.model.ItemStatus;
import com.escola.biblioteca.domain.repository.ItemRepository;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/items")
public class ItemController {

    private final ItemRepository itemRepository;
    private final DashboardPublisher dashboardPublisher;

    public ItemController(ItemRepository itemRepository, DashboardPublisher dashboardPublisher) {
        this.itemRepository = itemRepository;
        this.dashboardPublisher = dashboardPublisher;
    }

    @GetMapping
    public List<Item> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) UUID workId) {
        if (workId != null) {
            return itemRepository.findByWorkId(workId);
        }
        var all = new java.util.ArrayList<Item>();
        itemRepository.findAll().forEach(all::add);
        return all;
    }

    @PostMapping
    public ResponseEntity<Item> criar(@PathVariable UUID institutionId, @RequestBody ItemRequest request) {
        Item item = new Item();
        item.setWorkId(request.workId());
        item.setLibraryId(request.libraryId());
        item.setBarcode(request.barcode());
        item.setCallNumber(request.callNumber());
        item.setStatus(ItemStatus.AVAILABLE);
        item.marcarNovo();
        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Item atualizar(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setWorkId(request.workId());
        item.setLibraryId(request.libraryId());
        item.setBarcode(request.barcode());
        item.setCallNumber(request.callNumber());
        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        itemRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<Item> transferir(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody TransferRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setLibraryId(request.targetLibraryId());
        item.setStatus(ItemStatus.IN_TRANSIT);
        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<Item> receber(@PathVariable UUID institutionId, @PathVariable UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setStatus(ItemStatus.AVAILABLE);
        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Item> darBaixa(@PathVariable UUID institutionId, @PathVariable UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setStatus(ItemStatus.WITHDRAWN);
        Item saved = itemRepository.save(item);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.ok(saved);
    }

    public record ItemRequest(UUID workId, UUID libraryId, String barcode, String callNumber) {}
    public record TransferRequest(UUID targetLibraryId) {}
}
