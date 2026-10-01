package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.circulation.model.Fine;
import com.escola.biblioteca.domain.circulation.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @GetMapping
    public List<Fine> listar(@RequestParam(required = false) UUID patronId) {
        if (patronId != null) {
            return fineService.findPendingByPatron(patronId);
        }
        return fineService.findAll();
    }

    @PostMapping("/{fineId}/pay")
    public Fine pagar(@PathVariable UUID fineId, @RequestBody PayRequest request) {
        return fineService.pay(fineId, request.paymentRef(), request.amountCents());
    }

    @PostMapping("/{fineId}/waive")
    public Fine isentar(@PathVariable UUID fineId, @RequestBody WaiveRequest request) {
        return fineService.waive(fineId, request.staffId(), request.reason());
    }

    @PostMapping("/{fineId}/write-off")
    public Fine darBaixa(@PathVariable UUID fineId, @RequestBody WaiveRequest request) {
        return fineService.writeOff(fineId, request.staffId(), request.reason());
    }

    @PostMapping("/patrons/{patronId}/recalculate-fines")
    public ResponseEntity<Void> recalcularSaldo(@PathVariable UUID patronId) {
        fineService.recalculateBalance(patronId);
        return ResponseEntity.ok().build();
    }

    public record PayRequest(String paymentRef, int amountCents) {}
    public record WaiveRequest(UUID staffId, String reason) {}
}
