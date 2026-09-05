package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.AutorRequest;
import com.escola.biblioteca.dto.response.AutorResponse;
import com.escola.biblioteca.service.AutorService;
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
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @GetMapping
    public List<AutorResponse> listar(@RequestParam(required = false) String nome) {
        return autorService.listar(nome);
    }

    @PostMapping
    public ResponseEntity<AutorResponse> salvar(@Valid @RequestBody AutorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autorService.salvar(request));
    }

    @PutMapping("/{id}")
    public AutorResponse atualizar(@PathVariable Integer id, @Valid @RequestBody AutorRequest request) {
        return autorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Integer id) {
        autorService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
