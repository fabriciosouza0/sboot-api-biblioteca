package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Fine;
import com.escola.biblioteca.domain.model.FineStatus;
import com.escola.biblioteca.domain.repository.FineRepository;
import com.escola.biblioteca.domain.service.FineService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/fines")
public class FineController {

    private final FineService fineService;
    private final FineRepository fineRepository;

    public FineController(FineService fineService, FineRepository fineRepository) {
        this.fineService = fineService;
        this.fineRepository = fineRepository;
    }

    @GetMapping
    public List<Fine> listar(@PathVariable UUID institutionId,
                             @RequestParam(required = false) String term,
                             @RequestParam(required = false) FineStatus status) {
        var all = new java.util.ArrayList<Fine>();
        fineRepository.findAll().forEach(all::add);
        if (status != null) {
            all.removeIf(f -> f.getStatus() != status);
        }
        return all;
    }

    @PostMapping("/{id}/pay")
    public Fine pagar(@PathVariable UUID institutionId, @PathVariable UUID id,
                      @RequestBody PayRequest request) {
        return fineService.pay(id, request.paymentRef(), request.amountCents());
    }

    @PostMapping("/{id}/waive")
    public Fine isentar(@PathVariable UUID institutionId, @PathVariable UUID id,
                        @RequestBody ReasonRequest request) {
        return fineService.waive(id, UUID.randomUUID(), request.reason());
    }

    @PostMapping("/{id}/write-off")
    public ResponseEntity<Fine> baixar(@PathVariable UUID institutionId, @PathVariable UUID id,
                                       @RequestBody ReasonRequest request) {
        Fine fine = fineService.writeOff(id, UUID.randomUUID(), request.reason());
        return ResponseEntity.ok(fine);
    }

    public record PayRequest(String paymentRef, Integer amountCents) {}
    public record ReasonRequest(String reason) {}
}
