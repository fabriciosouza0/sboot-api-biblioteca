package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.model.Hold;
import com.escola.biblioteca.domain.repository.HoldRepository;
import com.escola.biblioteca.domain.service.HoldService;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/holds")
public class HoldController {

    private final HoldRepository holdRepository;
    private final HoldService holdService;
    private final DashboardPublisher dashboardPublisher;

    public HoldController(HoldRepository holdRepository, HoldService holdService,
                          DashboardPublisher dashboardPublisher) {
        this.holdRepository = holdRepository;
        this.holdService = holdService;
        this.dashboardPublisher = dashboardPublisher;
    }

    @GetMapping
    public List<Hold> listar(@PathVariable UUID institutionId) {
        var all = new java.util.ArrayList<Hold>();
        holdRepository.findAll().forEach(all::add);
        return all;
    }

    @PostMapping
    public ResponseEntity<Hold> criar(@PathVariable UUID institutionId, @RequestBody HoldRequest request) {
        Hold hold = holdService.placeHold(request.patronId(), request.workId(), request.libraryId());
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.status(HttpStatus.CREATED).body(hold);
    }

    @DeleteMapping("/{holdId}")
    public ResponseEntity<Void> cancelar(@PathVariable UUID institutionId, @PathVariable UUID holdId) {
        holdService.cancelHold(holdId);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{holdId}/fulfill")
    public Hold cumprir(@PathVariable UUID institutionId, @PathVariable UUID holdId) {
        Hold fulfilled = holdService.fulfillHold(holdId);
        dashboardPublisher.dadosAlterados();
        return fulfilled;
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expirar(@PathVariable UUID institutionId) {
        // TODO: Implement hold expiration scheduler
        return ResponseEntity.ok().build();
    }

    public record HoldRequest(UUID patronId, UUID workId, UUID libraryId) {}
}
