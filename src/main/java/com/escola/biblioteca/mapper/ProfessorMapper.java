package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.ProfessorRequest;
import com.escola.biblioteca.model.Locatario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {

    @Mapping(target = "codigoProfessor", ignore = true)
    @Mapping(target = "codigoAluno", ignore = true)
    @Mapping(target = "novo", ignore = true)
    Locatario toLocatario(ProfessorRequest request);

    @Mapping(target = "codigoProfessor", ignore = true)
    @Mapping(target = "codigoAluno", ignore = true)
    @Mapping(target = "novo", ignore = true)
    void update(ProfessorRequest request, @MappingTarget Locatario locatario);
}