package com.escola.biblioteca.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com dados de um aluno")
public record AlunoResponse(
        @Schema(description = "CPF do aluno", example = "12345678900")
        String cpf,
        @Schema(description = "Nome completo do aluno", example = "João da Silva")
        String nome,
        @Schema(description = "Telefone com DDD", example = "11999998888")
        String telefone,
        @Schema(description = "Código sequencial do aluno", example = "1")
        Integer codigo,
        @Schema(description = "Código da turma", example = "1")
        Integer codigoTurma,
        @Schema(description = "Nome da turma", example = "3º Ano A")
        String turma) {
}
