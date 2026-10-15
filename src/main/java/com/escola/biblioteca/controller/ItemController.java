package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.service.ItemService;
import com.escola.biblioteca.dto.request.ItemRequest;
import com.escola.biblioteca.dto.request.TransferRequest;
import com.escola.biblioteca.dto.request.WithdrawRequest;
import lombok.RequiredArgsConstructor;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import com.escola.biblioteca.dto.response.ItemResponse;
import com.escola.biblioteca.security.AdminAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public Page<ItemResponse> listar(
            @RequestParam(required = false) UUID workId,
            @RequestParam(required = false) UUID libraryId,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String term,
            @PageableDefault(size = 20) Pageable pageable) {
        return itemService.findAllWithDetails(workId, libraryId, status, term, pageable);
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
        return ResponseEntity.ok(itemService.transfer(id, request.targetLibraryId(), request.transferNote()));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<Item> receber(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Library-Id", required = false) UUID activeLibraryId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isGlobalAdmin = auth instanceof AdminAuthenticationToken adminAuth && adminAuth.isGlobalAdmin();
        return ResponseEntity.ok(itemService.receive(id, activeLibraryId, isGlobalAdmin));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Item> darBaixa(@PathVariable UUID id, @RequestBody(required = false) WithdrawRequest request) {
        String reason = (request != null) ? request.reason() : null;
        return ResponseEntity.ok(itemService.withdraw(id, reason));
    }
}
