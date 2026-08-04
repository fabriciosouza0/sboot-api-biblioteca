package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.ProfessorRequest;
import com.escola.biblioteca.dto.response.ProfessorResponse;
import com.escola.biblioteca.model.Locatario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {

    @Mapping(target = "codigo", expression = "java(locatario.getProfessor() != null ? locatario.getProfessor().getId() : null)")
    ProfessorResponse toResponse(Locatario locatario);

    @Mapping(target = "aluno", ignore = true)
    @Mapping(target = "professor", ignore = true)
    Locatario toLocatario(ProfessorRequest request);

    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "aluno", ignore = true)
    @Mapping(target = "professor", ignore = true)
    void update(ProfessorRequest request, @MappingTarget Locatario locatario);
}