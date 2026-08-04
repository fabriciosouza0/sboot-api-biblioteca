package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.LivroRequest;
import com.escola.biblioteca.dto.response.LivroResponse;
import com.escola.biblioteca.service.LivroService;
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
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @GetMapping
    public List<LivroResponse> listar(@RequestParam(required = false) String titulo) {
        return livroService.listar(titulo);
    }

    @PostMapping
    public ResponseEntity<LivroResponse> salvar(@Valid @RequestBody LivroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livroService.salvar(request));
    }

    @PutMapping("/{codigo}")
    public LivroResponse atualizar(@PathVariable Long codigo, @Valid @RequestBody LivroRequest request) {
        return livroService.atualizar(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> remover(@PathVariable Long codigo) {
        livroService.remover(codigo);
        return ResponseEntity.noContent().build();
    }
}
