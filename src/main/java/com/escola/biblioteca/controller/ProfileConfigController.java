package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.ProfileConfig;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import com.escola.biblioteca.exception.ResourceNotFoundException;
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
    public List<ProfileConfig> listar(@PathVariable UUID institutionId) {
        var all = new java.util.ArrayList<ProfileConfig>();
        profileConfigRepository.findAll().forEach(pc -> {
            if (pc.getInstitutionId().equals(institutionId)) all.add(pc);
        });
        return all;
    }

    @PostMapping
    public ResponseEntity<ProfileConfig> criar(@PathVariable UUID institutionId, @RequestBody ProfileConfigRequest request) {
        ProfileConfig config = new ProfileConfig();
        config.setInstitutionId(institutionId);
        config.setProfile(PatronProfile.valueOf(request.profile()));
        config.setMaxLoans(request.maxLoans());
        config.setLoanDays(request.loanDays());
        config.setMaxRenewals(request.maxRenewals());
        config.setHoldLimit(request.holdLimit());
        config.setFineRateCents(request.fineRateCents());
        config.setFineCapCents(request.fineCapCents());
        config.marcarNovo();
        return ResponseEntity.status(HttpStatus.CREATED).body(profileConfigRepository.save(config));
    }

    @PutMapping("/{id}")
    public ProfileConfig atualizar(@PathVariable UUID institutionId, @PathVariable UUID id, @RequestBody ProfileConfigRequest request) {
        ProfileConfig config = profileConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.profile_config"));
        config.setProfile(PatronProfile.valueOf(request.profile()));
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

    public record ProfileConfigRequest(String profile, int maxLoans, int loanDays, int maxRenewals,
                                       int holdLimit, int fineRateCents, int fineCapCents) {}
}
