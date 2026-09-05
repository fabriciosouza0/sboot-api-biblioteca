package com.escola.biblioteca.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.dto.request.ProfessorRequest;
import com.escola.biblioteca.dto.response.ProfessorResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.mapper.ProfessorMapper;
import com.escola.biblioteca.model.Locatario;
import com.escola.biblioteca.model.Professor;
import com.escola.biblioteca.repository.LocatarioRepository;
import com.escola.biblioteca.repository.ProfessorRepository;
import com.escola.biblioteca.repository.query.LocatarioQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final LocatarioRepository locatarioRepository;
    private final LocatarioQueryRepository locatarioQueryRepository;
    private final ProfessorMapper professorMapper;
    private final DashboardPublisher dashboardPublisher;

    public List<ProfessorResponse> listar(String nome) {
        String term = (nome == null || nome.isBlank()) ? null : nome.trim();
        return locatarioQueryRepository.buscarProfessores(term);
    }

    @Transactional
    public ProfessorResponse salvar(ProfessorRequest request) {
        if (locatarioRepository.existsById(request.cpf())) {
            throw new BusinessException("error.duplicado.cpf");
        }
        Professor professor = professorRepository.save(new Professor());

        Locatario locatario = professorMapper.toLocatario(request);
        locatario.setCodigoProfessor(professor.getCodigo());
        locatario.marcarNovo();
        locatarioRepository.save(locatario);

        dashboardPublisher.dadosAlterados();
        return new ProfessorResponse(
                professor.getCodigo(),
                locatario.getNome(),
                locatario.getCpf());
    }

    @Transactional
    public ProfessorResponse atualizar(String cpf, ProfessorRequest request) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.professor"));
        if (locatario.getCodigoProfessor() == null) {
            throw new BusinessException("error.cpf.nao.professor");
        }
        professorMapper.update(request, locatario);
        locatarioRepository.save(locatario);
        dashboardPublisher.dadosAlterados();
        return locatarioQueryRepository.buscarProfessorPorCpf(cpf)
                .orElseThrow(() -> new IllegalStateException("Professor não encontrado após atualizar"));
    }

    @Transactional
    public void remover(String cpf) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.professor"));
        if (locatario.getCodigoProfessor() == null) {
            throw new BusinessException("error.cpf.nao.professor");
        }
        Integer codigoProfessor = locatario.getCodigoProfessor();
        locatarioRepository.delete(locatario);
        professorRepository.deleteById(codigoProfessor);
        dashboardPublisher.dadosAlterados();
    }
}