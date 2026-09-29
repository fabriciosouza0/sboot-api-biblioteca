package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.repository.PatronRepository;
import com.escola.biblioteca.domain.repository.query.PatronQueryRepository;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/patrons")
public class PatronController {

    private final PatronRepository patronRepository;
    private final PatronQueryRepository patronQueryRepository;
    private final DashboardPublisher dashboardPublisher;

    public PatronController(PatronRepository patronRepository, PatronQueryRepository patronQueryRepository,
                            DashboardPublisher dashboardPublisher) {
        this.patronRepository = patronRepository;
        this.patronQueryRepository = patronQueryRepository;
        this.dashboardPublisher = dashboardPublisher;
    }

    @GetMapping
    public List<Patron> listar(@PathVariable UUID institutionId,
                               @RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return patronQueryRepository.searchByName(institutionId, name);
        }
        return patronRepository.findByInstitutionId(institutionId);
    }

    @PostMapping
    public ResponseEntity<Patron> criar(@PathVariable UUID institutionId, @RequestBody PatronRequest request) {
        Patron patron = new Patron();
        patron.setInstitutionId(institutionId);
        patron.setExternalId(request.externalId());
        patron.setName(request.name());
        patron.setPhone(request.phone());
        patron.setProfile(PatronProfile.valueOf(request.profile()));
        patron.marcarNovo();
        Patron saved = patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Patron atualizar(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody PatronRequest request) {
        Patron patron = patronRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.patron"));
        patron.setExternalId(request.externalId());
        patron.setName(request.name());
        patron.setPhone(request.phone());
        patron.setProfile(PatronProfile.valueOf(request.profile()));
        Patron saved = patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        patronRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
        return ResponseEntity.noContent().build();
    }

    public record PatronRequest(String externalId, String name, String phone, String profile) {}
}
