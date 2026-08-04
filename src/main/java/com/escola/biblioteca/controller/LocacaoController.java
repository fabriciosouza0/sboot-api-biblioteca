package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.LocacaoRequest;
import com.escola.biblioteca.dto.response.LocacaoResponse;
import com.escola.biblioteca.service.LocacaoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locacoes")
public class LocacaoController {

    private final LocacaoService locacaoService;

    public LocacaoController(LocacaoService locacaoService) {
        this.locacaoService = locacaoService;
    }

    @GetMapping
    public List<LocacaoResponse> listar(@RequestParam(required = false) String locatario) {
        return locacaoService.listar(locatario);
    }

    @PostMapping
    public ResponseEntity<LocacaoResponse> salvar(@Valid @RequestBody LocacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoService.salvar(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> devolver(@PathVariable Integer id) {
        locacaoService.devolver(id);
        return ResponseEntity.noContent().build();
    }
}
