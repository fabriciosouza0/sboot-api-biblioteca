package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.model.ProfileConfig;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions/{institutionId}/profile-configs")
public class ProfileConfigController {

    private final ProfileConfigRepository profileConfigRepository;

    public ProfileConfigController(ProfileConfigRepository profileConfigRepository) {
        this.profileConfigRepository = profileConfigRepository;
    }

    @GetMapping
    public List<ProfileConfig> listar(@PathVariable UUID institutionId,
                                      @RequestParam(required = false) String term) {
        var all = new java.util.ArrayList<ProfileConfig>();
        profileConfigRepository.findAll().forEach(all::add);
        return all.stream()
                .filter(c -> c.getInstitutionId().equals(institutionId))
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProfileConfig> criar(@PathVariable UUID institutionId,
                                               @RequestBody ProfileConfigRequest request) {
        ProfileConfig config = new ProfileConfig();
        config.setInstitutionId(institutionId);
        config.setProfile(request.profile());
        config.setMaxLoans(request.maxLoans());
        config.setLoanDays(request.loanDays());
        config.setMaxRenewals(request.maxRenewals());
        config.setHoldLimit(request.holdLimit());
        config.setFineRateCents(request.fineRateCents());
        config.setFineCapCents(request.fineCapCents());
        config.marcarNovo();
        ProfileConfig saved = profileConfigRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ProfileConfig atualizar(@PathVariable UUID institutionId, @PathVariable UUID id,
                                   @RequestBody ProfileConfigRequest request) {
        ProfileConfig config = profileConfigRepository.findById(id).orElseThrow(() ->
                new com.escola.biblioteca.exception.ResourceNotFoundException("error.notfound.profileConfig"));
        config.setProfile(request.profile());
        config.setMaxLoans(request.maxLoans());
        config.setLoanDays(request.loanDays());
        config.setMaxRenewals(request.maxRenewals());
        config.setHoldLimit(request.holdLimit());
        config.setFineRateCents(request.fineRateCents());
        config.setFineCapCents(request.fineCapCents());
        return profileConfigRepository.save(config);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID institutionId, @PathVariable UUID id) {
        profileConfigRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record ProfileConfigRequest(PatronProfile profile, Integer maxLoans, Integer loanDays,
                                       Integer maxRenewals, Integer holdLimit,
                                       Integer fineRateCents, Integer fineCapCents) {}
}
