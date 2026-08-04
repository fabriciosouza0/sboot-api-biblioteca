package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.response.LocacaoResponse;
import com.escola.biblioteca.model.Loca;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocacaoMapper {

    @Mapping(target = "codigo", source = "loca.id")
    @Mapping(target = "codigoLivro", source = "loca.livro.id")
    @Mapping(target = "livro", source = "loca.livro.titulo")
    @Mapping(target = "cpfLocatario", source = "loca.locatario.cpf")
    @Mapping(target = "locatario", source = "loca.locatario.nome")
    @Mapping(target = "atrasado", source = "atrasado")
    @Mapping(target = "dias", source = "dias")
    LocacaoResponse toResponse(Loca loca, boolean atrasado, long dias);
}