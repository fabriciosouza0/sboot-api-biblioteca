package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Hold;
import com.escola.biblioteca.domain.model.HoldStatus;
import com.escola.biblioteca.domain.repository.HoldRepository;
import com.escola.biblioteca.domain.service.HoldService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/holds")
public class HoldController {

    private final HoldService holdService;
    private final HoldRepository holdRepository;

    public HoldController(HoldService holdService, HoldRepository holdRepository) {
        this.holdService = holdService;
        this.holdRepository = holdRepository;
    }

    @GetMapping
    public List<Hold> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) String term,
                             @RequestParam(required = false) HoldStatus status) {
        var all = new java.util.ArrayList<Hold>();
        holdRepository.findAll().forEach(all::add);
        if (status != null) {
            all.removeIf(h -> h.getStatus() != status);
        }
        return all;
    }

    @PostMapping
    public ResponseEntity<Hold> criar(@PathVariable UUID institutionId, @RequestBody HoldRequest request) {
        Hold hold = holdService.placeHold(request.patronId(), request.workId(), request.libraryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(hold);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID institutionId, @PathVariable UUID id) {
        holdService.cancelHold(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/fulfill")
    public Hold cumprir(@PathVariable UUID institutionId, @PathVariable UUID id) {
        return holdService.fulfillHold(id);
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expirar(@PathVariable UUID institutionId) {
        // TODO: implement HoldService.expireHolds()
        return ResponseEntity.ok().build();
    }

    public record HoldRequest(UUID patronId, UUID workId, UUID libraryId) {}
}
