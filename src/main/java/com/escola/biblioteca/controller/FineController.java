package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.model.Fine;
import com.escola.biblioteca.domain.repository.FineRepository;
import com.escola.biblioteca.domain.service.FineService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/fines")
public class FineController {

    private final FineRepository fineRepository;
    private final FineService fineService;
    private final DashboardPublisher dashboardPublisher;

    public FineController(FineRepository fineRepository, FineService fineService,
                          DashboardPublisher dashboardPublisher) {
        this.fineRepository = fineRepository;
        this.fineService = fineService;
        this.dashboardPublisher = dashboardPublisher;
    }

    @GetMapping
    public List<Fine> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) UUID patronId) {
        if (patronId != null) {
            return fineService.findPendingByPatron(patronId);
        }
        var all = new java.util.ArrayList<Fine>();
        fineRepository.findAll().forEach(all::add);
        return all;
    }

    @PostMapping("/{fineId}/pay")
    public Fine pagar(@PathVariable UUID institutionId, @PathVariable UUID fineId,
                      @RequestBody PayRequest request) {
        Fine paid = fineService.pay(fineId, request.paymentRef(), request.amountCents());
        dashboardPublisher.dadosAlterados();
        return paid;
    }

    @PostMapping("/{fineId}/waive")
    public Fine isentar(@PathVariable UUID institutionId, @PathVariable UUID fineId,
                        @RequestBody WaiveRequest request) {
        Fine waived = fineService.waive(fineId, request.staffId(), request.reason());
        dashboardPublisher.dadosAlterados();
        return waived;
    }

    @PostMapping("/{fineId}/write-off")
    public Fine darBaixa(@PathVariable UUID institutionId, @PathVariable UUID fineId,
                         @RequestBody WaiveRequest request) {
        Fine writtenOff = fineService.writeOff(fineId, request.staffId(), request.reason());
        dashboardPublisher.dadosAlterados();
        return writtenOff;
    }

    @PostMapping("/patrons/{patronId}/recalculate-fines")
    public ResponseEntity<Void> recalcularSaldo(@PathVariable UUID institutionId, @PathVariable UUID patronId) {
        fineService.recalculateBalance(patronId);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.ok().build();
    }

    public record PayRequest(String paymentRef, int amountCents) {}
    public record WaiveRequest(UUID staffId, String reason) {}
}
