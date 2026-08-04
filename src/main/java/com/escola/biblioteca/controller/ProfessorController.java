package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.ProfessorRequest;
import com.escola.biblioteca.dto.response.ProfessorResponse;
import com.escola.biblioteca.service.ProfessorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping
    public List<ProfessorResponse> listar(@RequestParam(required = false) String nome) {
        return professorService.listar(nome);
    }

    @PostMapping
    public ResponseEntity<ProfessorResponse> salvar(@Valid @RequestBody ProfessorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professorService.salvar(request));
    }

    @PutMapping("/{cpf}")
    public ProfessorResponse atualizar(@PathVariable String cpf, @Valid @RequestBody ProfessorRequest request) {
        return professorService.atualizar(cpf, request);
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> remover(@PathVariable String cpf) {
        professorService.remover(cpf);
        return ResponseEntity.noContent().build();
    }
}
