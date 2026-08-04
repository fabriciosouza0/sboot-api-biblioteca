package com.escola.biblioteca.dto.response;

public record LivroResponse(
        Long codigo,
        String titulo,
        Integer qtd,
        Integer codigoAutor,
        String autor,
        Long codigoCDD,
        String cdd) {
}
