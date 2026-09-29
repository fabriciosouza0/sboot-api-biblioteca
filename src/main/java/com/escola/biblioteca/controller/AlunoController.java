package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.AlunoRequest;
import com.escola.biblioteca.dto.response.AlunoResponse;
import com.escola.biblioteca.dto.response.ApiError;
import com.escola.biblioteca.service.AlunoService;
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
@RequestMapping("/api/alunos")
@Tag(name = "Alunos", description = "CRUD de alunos da instituição")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @GetMapping
    @Operation(summary = "Listar alunos", description = "Retorna todos os alunos. Filtra por nome quando informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de alunos",
                    content = @Content(schema = @Schema(implementation = AlunoResponse.class)))
    })
    public List<AlunoResponse> listar(
            @Parameter(description = "Filtro por nome (parcial, case-insensitive)")
            @RequestParam(required = false) String nome) {
        return alunoService.listar(nome);
    }

    @PostMapping
    @Operation(summary = "Cadastrar aluno", description = "Cadastra um novo aluno na instituição.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Aluno cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlunoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou aluno já existente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AlunoResponse> salvar(@Valid @RequestBody AlunoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alunoService.salvar(request));
    }

    @PutMapping("/{cpf}")
    @Operation(summary = "Atualizar aluno", description = "Atualiza os dados de um aluno existente pelo CPF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aluno atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AlunoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AlunoResponse atualizar(
            @Parameter(description = "CPF do aluno (somente dígitos)") @PathVariable String cpf,
            @Valid @RequestBody AlunoRequest request) {
        return alunoService.atualizar(cpf, request);
    }

    @DeleteMapping("/{cpf}")
    @Operation(summary = "Remover aluno", description = "Remove um aluno da instituição pelo CPF.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aluno removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aluno não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(
            @Parameter(description = "CPF do aluno (somente dígitos)") @PathVariable String cpf) {
        alunoService.remover(cpf);
        return ResponseEntity.noContent().build();
    }
}
