package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.LivroRequest;
import com.escola.biblioteca.dto.response.LivroResponse;
import com.escola.biblioteca.model.Autor;
import com.escola.biblioteca.model.Cdd;
import com.escola.biblioteca.model.Livro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LivroMapper {

    @Mapping(target = "codigo", source = "id")
    @Mapping(target = "codigoAutor", expression = "java(livro.getAutor() != null ? livro.getAutor().getId() : null)")
    @Mapping(target = "autor", expression = "java(livro.getAutor() != null ? livro.getAutor().getNome() : null)")
    @Mapping(target = "codigoCDD", expression = "java(livro.getCdd() != null ? livro.getCdd().getId() : null)")
    @Mapping(target = "cdd", expression = "java(livro.getCdd() != null ? livro.getCdd().getDescricao() : null)")
    LivroResponse toResponse(Livro livro);

    @Mapping(target = "id", source = "request.codigo")
    @Mapping(target = "autor", source = "autor")
    @Mapping(target = "cdd", source = "cdd")
    Livro toEntity(LivroRequest request, Autor autor, Cdd cdd);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", source = "autor")
    @Mapping(target = "cdd", source = "cdd")
    void updateEntity(LivroRequest request, Autor autor, Cdd cdd, @MappingTarget Livro livro);
}