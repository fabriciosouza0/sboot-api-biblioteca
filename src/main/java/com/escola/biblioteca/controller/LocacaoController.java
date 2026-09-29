package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.LocacaoRequest;
import com.escola.biblioteca.dto.response.ApiError;
import com.escola.biblioteca.dto.response.LocacaoResponse;
import com.escola.biblioteca.service.LocacaoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locacoes")
@Tag(name = "Locações", description = "Registro e devolução de locações de livros")
public class LocacaoController {

    private final LocacaoService locacaoService;

    public LocacaoController(LocacaoService locacaoService) {
        this.locacaoService = locacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar locações", description = "Retorna todas as locações. Filtra por nome do locatário quando informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de locações",
                    content = @Content(schema = @Schema(implementation = LocacaoResponse.class)))
    })
    public List<LocacaoResponse> listar(
            @Parameter(description = "Filtro por nome do locatário (parcial, case-insensitive)")
            @RequestParam(required = false) String locatario) {
        return locacaoService.listar(locatario);
    }

    @PostMapping
    @Operation(summary = "Registrar locação", description = "Registra uma nova locação de livro para um aluno.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Locação registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = LocacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, livro indisponível ou aluno inexistente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LocacaoResponse> salvar(@Valid @RequestBody LocacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoService.salvar(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Devolver livro", description = "Registra a devolução de um livro locado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Devolução registrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Locação não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> devolver(
            @Parameter(description = "Código da locação") @PathVariable Integer id) {
        locacaoService.devolver(id);
        return ResponseEntity.noContent().build();
    }
}
