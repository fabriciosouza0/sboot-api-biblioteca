package com.escola.biblioteca.dto.response;

import java.time.LocalDate;

public record LocacaoResponse(
        Integer codigo,
        Long codigoLivro,
        String livro,
        String cpfLocatario,
        String locatario,
        LocalDate dataDeLocacao,
        LocalDate dataParaDevolucao,
        boolean atrasado,
        long dias) {
}
