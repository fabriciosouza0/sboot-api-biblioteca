package com.escola.biblioteca.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requisição para criar ou atualizar um aluno")
public record AlunoRequest(
        @NotBlank(message = "{validation.aluno.cpf}")
        @Pattern(regexp = "\\d{11}", message = "{validation.cpf.invalid}")
        @Schema(description = "CPF do aluno (somente dígitos, 11 caracteres)", example = "12345678900")
        String cpf,
        @NotBlank(message = "{validation.aluno.nome}")
        @Size(max = 45, message = "{validation.size}")
        @Schema(description = "Nome completo do aluno (máx. 45 caracteres)", example = "João da Silva")
        String nome,
        @Pattern(regexp = "\\d{10,11}", message = "{validation.telefone.invalid}")
        @Schema(description = "Telefone com DDD (10-11 dígitos, somente números)", example = "11999998888")
        String telefone,
        @NotNull(message = "{validation.aluno.turma}")
        @Schema(description = "Código da turma (FK)", example = "1")
        Integer codigoTurma) {
}
