package com.escola.biblioteca.dto.response;

public record AlunoResponse(
        String cpf,
        String nome,
        String telefone,
        Integer codigo,
        Integer codigoTurma,
        String turma) {
}
