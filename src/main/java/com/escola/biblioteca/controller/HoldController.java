package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.circulation.model.Hold;
import com.escola.biblioteca.domain.circulation.service.HoldService;
import com.escola.biblioteca.dto.response.HoldResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/holds")
@RequiredArgsConstructor
public class HoldController {

    private final HoldService holdService;

    @GetMapping
    public Page<HoldResponse> listar(
            @RequestParam(required = false) UUID patronId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String term,
            @PageableDefault(size = 20) Pageable pageable) {
        return holdService.findAllWithDetails(patronId, status, term, pageable);
    }

    @PostMapping
    public ResponseEntity<Hold> criar(@RequestBody HoldRequest request) {
        Hold hold = holdService.placeHold(request.patronId(), request.workId(), request.libraryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(hold);
    }

    @DeleteMapping("/{holdId}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID holdId) {
        holdService.cancelHold(holdId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{holdId}/fulfill")
    public Hold cumprir(@PathVariable UUID holdId) {
        return holdService.fulfillHold(holdId);
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expirar() {
        // TODO: Implement hold expiration scheduler
        return ResponseEntity.ok().build();
    }

    public record HoldRequest(UUID patronId, UUID workId, UUID libraryId) {}
}
