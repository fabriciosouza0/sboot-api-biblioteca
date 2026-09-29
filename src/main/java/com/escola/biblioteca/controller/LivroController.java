package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.LivroRequest;
import com.escola.biblioteca.dto.response.ApiError;
import com.escola.biblioteca.dto.response.LivroResponse;
import com.escola.biblioteca.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Livros", description = "CRUD de livros do acervo bibliotecário")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @GetMapping
    @Operation(summary = "Listar livros", description = "Retorna todos os livros. Filtra por título quando informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de livros",
                    content = @Content(schema = @Schema(implementation = LivroResponse.class)))
    })
    public List<LivroResponse> listar(
            @Parameter(description = "Filtro por título (parcial, case-insensitive)")
            @RequestParam(required = false) String titulo) {
        return livroService.listar(titulo);
    }

    @PostMapping
    @Operation(summary = "Cadastrar livro", description = "Cadastra um novo livro no acervo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = LivroResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou livro já existente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LivroResponse> salvar(@Valid @RequestBody LivroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livroService.salvar(request));
    }

    @PutMapping("/{codigo}")
    @Operation(summary = "Atualizar livro", description = "Atualiza os dados de um livro existente pelo ISBN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LivroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public LivroResponse atualizar(
            @Parameter(description = "ISBN do livro") @PathVariable Long codigo,
            @Valid @RequestBody LivroRequest request) {
        return livroService.atualizar(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    @Operation(summary = "Remover livro", description = "Remove um livro do acervo pelo ISBN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(
            @Parameter(description = "ISBN do livro") @PathVariable Long codigo) {
        livroService.remover(codigo);
        return ResponseEntity.noContent().build();
    }
}
