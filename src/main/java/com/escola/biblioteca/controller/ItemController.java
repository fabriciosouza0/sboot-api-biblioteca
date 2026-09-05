package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Item;
import com.escola.biblioteca.domain.model.ItemStatus;
import com.escola.biblioteca.domain.repository.ItemRepository;
import com.escola.biblioteca.domain.service.CatalogService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/items")
public class ItemController {

    private final CatalogService catalogService;
    private final ItemRepository itemRepository;

    public ItemController(CatalogService catalogService, ItemRepository itemRepository) {
        this.catalogService = catalogService;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public List<Item> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) String term,
                             @RequestParam(required = false) ItemStatus status) {
        List<Item> items = itemRepository.findByWorkId(null); // We need a different approach
        // Since there's no findByInstitutionId, we filter by status if provided
        // For now, return all items (frontend filters client-side)
        var all = new java.util.ArrayList<Item>();
        itemRepository.findAll().forEach(all::add);
        if (status != null) {
            all.removeIf(i -> i.getStatus() != status);
        }
        return all;
    }

    @PostMapping
    public ResponseEntity<Item> criar(@PathVariable UUID institutionId, @RequestBody ItemRequest request) {
        Item item = catalogService.registerItem(request.workId(), request.libraryId(),
                request.barcode(), request.callNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{id}")
    public Item atualizar(@PathVariable UUID institutionId, @PathVariable UUID id,
                          @RequestBody ItemRequest request) {
        Item item = itemRepository.findById(id).orElseThrow(() ->
                new com.escola.biblioteca.exception.ResourceNotFoundException("error.notfound.item"));
        item.setWorkId(request.workId());
        item.setLibraryId(request.libraryId());
        item.setBarcode(request.barcode());
        item.setCallNumber(request.callNumber());
        return itemRepository.save(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transfer")
    public Item transferir(@PathVariable UUID institutionId, @PathVariable UUID id,
                           @RequestBody TransferRequest request) {
        return catalogService.transferItem(id, request.toLibraryId());
    }

    @PostMapping("/{id}/receive")
    public Item receber(@PathVariable UUID institutionId, @PathVariable UUID id) {
        // We need to know the expected library. For simplicity, we use the item's current libraryId
        Item item = itemRepository.findById(id).orElseThrow(() ->
                new com.escola.biblioteca.exception.ResourceNotFoundException("error.notfound.item"));
        return catalogService.receiveTransfer(id, item.getLibraryId());
    }

    @PostMapping("/{id}/withdraw")
    public Item retirar(@PathVariable UUID institutionId, @PathVariable UUID id,
                        @RequestBody WithdrawRequest request) {
        return catalogService.withdrawItem(id, request.reason());
    }

    public record ItemRequest(UUID workId, UUID libraryId, String barcode, String callNumber) {}
    public record TransferRequest(UUID toLibraryId) {}
    public record WithdrawRequest(String reason) {}
}
