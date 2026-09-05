package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.AlunoRequest;
import com.escola.biblioteca.model.Locatario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlunoMapper {

    @Mapping(target = "cpf", source = "cpf")
    @Mapping(target = "codigoProfessor", ignore = true)
    @Mapping(target = "codigoAluno", ignore = true)
    @Mapping(target = "novo", ignore = true)
    Locatario toLocatario(AlunoRequest request);

    @Mapping(target = "codigoProfessor", ignore = true)
    @Mapping(target = "codigoAluno", ignore = true)
    @Mapping(target = "novo", ignore = true)
    void update(AlunoRequest request, @MappingTarget Locatario locatario);
}