package com.escola.biblioteca.controller;

import com.escola.biblioteca.domain.model.Institution;
import com.escola.biblioteca.domain.repository.InstitutionRepository;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    private final InstitutionRepository institutionRepository;

    public InstitutionController(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @GetMapping
    public List<Institution> listar() {
        var all = new java.util.ArrayList<Institution>();
        institutionRepository.findAll().forEach(all::add);
        return all;
    }

    @GetMapping("/{id}")
    public Institution buscarPorId(@PathVariable UUID id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.institution"));
    }

    @GetMapping("/by-code/{code}")
    public Institution buscarPorCode(@PathVariable String code) {
        return institutionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.institution"));
    }

    @PostMapping
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<Institution> criar(@RequestBody InstitutionRequest request) {
        if (institutionRepository.findByCode(request.code()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Institution institution = new Institution();
        institution.setCode(request.code());
        institution.setName(request.name());
        institution.marcarNovo();
        Institution saved = institutionRepository.save(institution);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public Institution atualizar(@PathVariable UUID id, @RequestBody InstitutionRequest request) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.institution"));
        institution.setName(request.name());
        return institutionRepository.save(institution);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        institutionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record InstitutionRequest(String code, String name) {}
}
