package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.CddRequest;
import com.escola.biblioteca.dto.request.CddUpdateRequest;
import com.escola.biblioteca.dto.response.CddResponse;
import com.escola.biblioteca.service.CddService;
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
@RequestMapping("/api/cdd")
public class CddController {

    private final CddService cddService;

    public CddController(CddService cddService) {
        this.cddService = cddService;
    }

    @GetMapping
    public List<CddResponse> listar(@RequestParam(required = false) String descricao) {
        return cddService.listar(descricao);
    }

    @PostMapping
    public ResponseEntity<CddResponse> salvar(@Valid @RequestBody CddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cddService.salvar(request));
    }

    @PutMapping("/{id}")
    public CddResponse atualizar(@PathVariable Long id, @Valid @RequestBody CddUpdateRequest request) {
        return cddService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cddService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
