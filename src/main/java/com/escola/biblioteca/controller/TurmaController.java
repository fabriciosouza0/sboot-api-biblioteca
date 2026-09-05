package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.TurmaRequest;
import com.escola.biblioteca.dto.response.TurmaResponse;
import com.escola.biblioteca.service.TurmaService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @GetMapping
    public List<TurmaResponse> listar() {
        return turmaService.listar();
    }

    @PostMapping
    public ResponseEntity<TurmaResponse> salvar(@Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.salvar(request));
    }

    @PutMapping("/{id}")
    public TurmaResponse atualizar(@PathVariable Integer id, @Valid @RequestBody TurmaRequest request) {
        return turmaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Integer id) {
        turmaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
