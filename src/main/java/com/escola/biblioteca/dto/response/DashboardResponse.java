package com.escola.biblioteca.dto.response;

public record DashboardResponse(
        long livros,
        long atrasados,
        long professores,
        long alunos,
        long locatarios,
        long autores,
        long cdds,
        long professoresComLivros,
        long alunosComLivros) {
}
