package com.escola.biblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AlunoRequest(
        @NotBlank(message = "{validation.aluno.cpf}")
        @Pattern(regexp = "\\d{11}", message = "{validation.cpf.invalid}") String cpf,
        @NotBlank(message = "{validation.aluno.nome}")
        @Size(max = 45, message = "{validation.size}") String nome,
        @Pattern(regexp = "\\d{10,11}", message = "{validation.telefone.invalid}") String telefone,
        @NotNull(message = "{validation.aluno.turma}") Integer codigoTurma) {
}
