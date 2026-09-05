package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.repository.PatronRepository;
import com.escola.biblioteca.domain.service.FineService;
import com.escola.biblioteca.domain.service.PatronService;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/patrons")
public class PatronController {

    private final PatronService patronService;
    private final PatronRepository patronRepository;
    private final FineService fineService;

    public PatronController(PatronService patronService, PatronRepository patronRepository, FineService fineService) {
        this.patronService = patronService;
        this.patronRepository = patronRepository;
        this.fineService = fineService;
    }

    @GetMapping
    public List<Patron> listar(@PathVariable UUID institutionId,
                               @RequestParam(required = false) String term) {
        if (term != null && !term.isBlank()) {
            return patronRepository.findByInstitutionId(institutionId).stream()
                    .filter(p -> p.getName().toLowerCase().contains(term.toLowerCase())
                            || p.getExternalId().contains(term))
                    .toList();
        }
        return patronRepository.findByInstitutionId(institutionId);
    }

    @PostMapping
    public ResponseEntity<Patron> criar(@PathVariable UUID institutionId, @RequestBody PatronRequest request) {
        Patron patron = patronService.register(institutionId, request.externalId(), request.name(),
                request.phone(), request.profile());
        return ResponseEntity.status(HttpStatus.CREATED).body(patron);
    }

    @PutMapping("/{id}")
    public Patron atualizar(@PathVariable UUID institutionId, @PathVariable UUID id,
                            @RequestBody PatronRequest request) {
        Patron patron = patronService.findById(id);
        patron.setName(request.name());
        patron.setPhone(request.phone());
        patron.setProfile(request.profile());
        return patronRepository.save(patron);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        patronRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recalculate-fines")
    public ResponseEntity<Void> recalculateFines(@PathVariable UUID institutionId, @PathVariable UUID id) {
        fineService.recalculateBalance(id);
        return ResponseEntity.ok().build();
    }

    public record PatronRequest(String externalId, String name, String phone, PatronProfile profile) {}
}
