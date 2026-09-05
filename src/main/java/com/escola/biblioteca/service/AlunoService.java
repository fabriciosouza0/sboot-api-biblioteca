package com.escola.biblioteca.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.dto.request.AlunoRequest;
import com.escola.biblioteca.dto.response.AlunoResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.mapper.AlunoMapper;
import com.escola.biblioteca.model.Aluno;
import com.escola.biblioteca.model.Locatario;
import com.escola.biblioteca.model.Turma;
import com.escola.biblioteca.repository.AlunoRepository;
import com.escola.biblioteca.repository.LocatarioRepository;
import com.escola.biblioteca.repository.TurmaRepository;
import com.escola.biblioteca.repository.query.LocatarioQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final LocatarioRepository locatarioRepository;
    private final TurmaRepository turmaRepository;
    private final LocatarioQueryRepository locatarioQueryRepository;
    private final AlunoMapper alunoMapper;
    private final DashboardPublisher dashboardPublisher;

    public List<AlunoResponse> listar(String nome) {
        String term = (nome == null || nome.isBlank()) ? null : nome.trim();
        return locatarioQueryRepository.buscarAlunos(term);
    }

    @Transactional
    public AlunoResponse salvar(AlunoRequest request) {
        if (locatarioRepository.existsById(request.cpf())) {
            throw new BusinessException("error.duplicado.cpf");
        }

        Turma turma = turmaRepository.findById(request.codigoTurma())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.turma"));

        Aluno aluno = alunoRepository.save(new Aluno(request.codigoTurma()));

        Locatario locatario = alunoMapper.toLocatario(request);
        locatario.setCodigoAluno(aluno.getCodigo());
        locatario.marcarNovo();
        locatarioRepository.save(locatario);

        dashboardPublisher.dadosAlterados();
        return new AlunoResponse(
                locatario.getCpf(),
                locatario.getNome(),
                locatario.getTelefone(),
                aluno.getCodigo(),
                request.codigoTurma(),
                turma.getDescricao());
    }

    @Transactional
    public AlunoResponse atualizar(String cpf, AlunoRequest request) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.aluno"));

        if (locatario.getCodigoAluno() == null) {
            throw new BusinessException("error.cpf.nao.aluno");
        }

        alunoMapper.update(request, locatario);
        Aluno aluno = alunoRepository.findById(locatario.getCodigoAluno())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.aluno"));

        if (!aluno.getCodigoTurma().equals(request.codigoTurma())) {
            Turma turma = turmaRepository.findById(request.codigoTurma())
                    .orElseThrow(() -> new ResourceNotFoundException("error.notfound.turma"));
            aluno.setCodigoTurma(turma.getCodigo());
            alunoRepository.save(aluno);
        }

        locatarioRepository.save(locatario);
        dashboardPublisher.dadosAlterados();

        return locatarioQueryRepository.buscarAlunoPorCpf(cpf)
                .orElseThrow(() -> new IllegalStateException("Aluno não encontrado após atualizar"));
    }

    @Transactional
    public void remover(String cpf) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.aluno"));

        if (locatario.getCodigoAluno() == null) {
            throw new BusinessException("error.cpf.nao.aluno");
        }

        Integer codigoAluno = locatario.getCodigoAluno();

        locatarioRepository.delete(locatario);
        alunoRepository.deleteById(codigoAluno);
        dashboardPublisher.dadosAlterados();
    }
}