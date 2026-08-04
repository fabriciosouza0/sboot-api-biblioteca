package com.escola.biblioteca.mapper;

import com.escola.biblioteca.dto.request.AlunoRequest;
import com.escola.biblioteca.dto.response.AlunoResponse;
import com.escola.biblioteca.model.Locatario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlunoMapper {

    @Mapping(target = "codigo", expression = "java(locatario.getAluno() != null ? locatario.getAluno().getId() : null)")
    @Mapping(target = "codigoTurma", expression = "java(locatario.getAluno() != null && locatario.getAluno().getTurma() != null ? locatario.getAluno().getTurma().getId() : null)")
    @Mapping(target = "turma", expression = "java(locatario.getAluno() != null && locatario.getAluno().getTurma() != null ? locatario.getAluno().getTurma().getDescricao() : null)")
    AlunoResponse toResponse(Locatario locatario);

    @Mapping(target = "aluno", ignore = true)
    @Mapping(target = "professor", ignore = true)
    Locatario toLocatario(AlunoRequest request);

    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "aluno", ignore = true)
    @Mapping(target = "professor", ignore = true)
    void update(AlunoRequest request, @MappingTarget Locatario locatario);
}