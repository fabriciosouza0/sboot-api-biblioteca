package com.escola.biblioteca.controller;

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

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(itemRepository.save(item));
    }

    @PutMapping("/{id}")
    public Item atualizar(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
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
    public ResponseEntity<Item> transferir(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody TransferRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setLibraryId(request.targetLibraryId());
        item.setStatus(ItemStatus.IN_TRANSIT);
        return ResponseEntity.ok(itemRepository.save(item));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<Item> receber(@PathVariable UUID institutionId, @PathVariable UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setStatus(ItemStatus.AVAILABLE);
        return ResponseEntity.ok(itemRepository.save(item));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Item> darBaixa(@PathVariable UUID institutionId, @PathVariable UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        item.setStatus(ItemStatus.WITHDRAWN);
        return ResponseEntity.ok(itemRepository.save(item));
    }

    public record ItemRequest(UUID workId, UUID libraryId, String barcode, String callNumber) {}
    public record TransferRequest(UUID targetLibraryId) {}
}
