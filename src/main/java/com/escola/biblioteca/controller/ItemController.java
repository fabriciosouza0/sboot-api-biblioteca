package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.service.ItemService;
import com.escola.biblioteca.dto.request.ItemRequest;
import com.escola.biblioteca.dto.request.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<Item> listar(@RequestParam(required = false) UUID workId) {
        if (workId != null) {
            return itemService.findByWorkId(workId);
        }
        return itemService.findAll();
    }

    @PostMapping
    public ResponseEntity<Item> criar(@RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    @PutMapping("/{id}")
    public Item atualizar(@PathVariable UUID id, @RequestBody ItemRequest request) {
        return itemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<Item> transferir(@PathVariable UUID id, @RequestBody TransferRequest request) {
        return ResponseEntity.ok(itemService.transfer(id, request.targetLibraryId()));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<Item> receber(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.receive(id));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Item> darBaixa(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.withdraw(id));
    }
}
