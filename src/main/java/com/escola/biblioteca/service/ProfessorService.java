package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.ProfessorRequest;
import com.escola.biblioteca.dto.response.ProfessorResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.mapper.ProfessorMapper;
import com.escola.biblioteca.model.Locatario;
import com.escola.biblioteca.model.Professor;
import com.escola.biblioteca.repository.LocatarioRepository;
import com.escola.biblioteca.repository.ProfessorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final LocatarioRepository locatarioRepository;
    private final ProfessorMapper professorMapper;

    public List<ProfessorResponse> listar(String nome) {
        List<Locatario> professores = (nome == null || nome.isBlank())
                ? locatarioRepository.findByProfessorIsNotNullOrderByNomeAsc()
                : locatarioRepository.searchProfessoresPorNome(nome.trim());
        return professores.stream().map(professorMapper::toResponse).toList();
    }

    @Transactional
    public ProfessorResponse salvar(ProfessorRequest request) {
        if (locatarioRepository.existsById(request.cpf())) {
            throw new BusinessException("error.duplicado.cpf");
        }
        Professor professor = professorRepository.save(new Professor());

        Locatario locatario = professorMapper.toLocatario(request);
        locatario.setProfessor(professor);
        locatarioRepository.save(locatario);
        return professorMapper.toResponse(locatario);
    }

    @Transactional
    public ProfessorResponse atualizar(String cpf, ProfessorRequest request) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.professor"));
        if (locatario.getProfessor() == null) {
            throw new BusinessException("error.cpf.nao.professor");
        }
        professorMapper.update(request, locatario);
        return professorMapper.toResponse(locatarioRepository.save(locatario));
    }

    @Transactional
    public void remover(String cpf) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.professor"));
        if (locatario.getProfessor() == null) {
            throw new BusinessException("error.cpf.nao.professor");
        }
        Integer idProfessor = locatario.getProfessor().getId();
        locatarioRepository.delete(locatario);
        professorRepository.deleteById(idProfessor);
    }

    public long count() {
        return professorRepository.count();
    }
}