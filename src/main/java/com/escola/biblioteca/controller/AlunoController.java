package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.AlunoRequest;
import com.escola.biblioteca.dto.response.AlunoResponse;
import com.escola.biblioteca.service.AlunoService;
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
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    public List<AlunoResponse> listar(@RequestParam(required = false) String nome) {
        return alunoService.listar(nome);
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> salvar(@Valid @RequestBody AlunoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alunoService.salvar(request));
    }

    @PutMapping("/{cpf}")
    public AlunoResponse atualizar(@PathVariable String cpf, @Valid @RequestBody AlunoRequest request) {
        return alunoService.atualizar(cpf, request);
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> remover(@PathVariable String cpf) {
        alunoService.remover(cpf);
        return ResponseEntity.noContent().build();
    }
}
