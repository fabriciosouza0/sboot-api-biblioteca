package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.patron.model.ProfileConfig;
import com.escola.biblioteca.domain.patron.service.ProfileConfigService;
import com.escola.biblioteca.dto.request.ProfileConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile-configs")
@RequiredArgsConstructor
public class ProfileConfigController {

    private final ProfileConfigService profileConfigService;

    @GetMapping
    public List<ProfileConfig> listar(@RequestHeader("X-Institution-Id") UUID institutionId) {
        return profileConfigService.findByInstitutionId(institutionId);
    }

    @PostMapping
    public ResponseEntity<ProfileConfig> criar(@RequestHeader("X-Institution-Id") UUID institutionId,
                                               @RequestBody ProfileConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileConfigService.create(institutionId, request));
    }

    @PutMapping("/{id}")
    public ProfileConfig atualizar(@PathVariable UUID id, @RequestBody ProfileConfigRequest request) {
        return profileConfigService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        profileConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
