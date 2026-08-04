package com.escola.biblioteca.service;

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
    private final AlunoMapper alunoMapper;

    public List<AlunoResponse> listar(String nome) {
        List<Locatario> alunos = (nome == null || nome.isBlank())
                ? locatarioRepository.findByAlunoIsNotNullOrderByNomeAsc()
                : locatarioRepository.searchAlunosPorNome(nome.trim());
        return alunos.stream().map(alunoMapper::toResponse).toList();
    }

    @Transactional
    public AlunoResponse salvar(AlunoRequest request) {
        if (locatarioRepository.existsById(request.cpf())) {
            throw new BusinessException("error.duplicado.cpf");
        }
        Turma turma = turmaRepository.findById(request.codigoTurma())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.turma"));

        Aluno aluno = alunoRepository.save(new Aluno(turma));

        Locatario locatario = alunoMapper.toLocatario(request);
        locatario.setAluno(aluno);
        locatarioRepository.save(locatario);
        return alunoMapper.toResponse(locatario);
    }

    @Transactional
    public AlunoResponse atualizar(String cpf, AlunoRequest request) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.aluno"));
        if (locatario.getAluno() == null) {
            throw new BusinessException("error.cpf.nao.aluno");
        }
        alunoMapper.update(request, locatario);
        if (!locatario.getAluno().getTurma().getId().equals(request.codigoTurma())) {
            Turma turma = turmaRepository.findById(request.codigoTurma())
                    .orElseThrow(() -> new ResourceNotFoundException("error.notfound.turma"));
            locatario.getAluno().setTurma(turma);
            alunoRepository.save(locatario.getAluno());
        }
        return alunoMapper.toResponse(locatarioRepository.save(locatario));
    }

    @Transactional
    public void remover(String cpf) {
        Locatario locatario = locatarioRepository.findById(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.aluno"));
        if (locatario.getAluno() == null) {
            throw new BusinessException("error.cpf.nao.aluno");
        }
        Integer idAluno = locatario.getAluno().getId();
        locatarioRepository.delete(locatario);
        alunoRepository.deleteById(idAluno);
    }

    public long count() {
        return alunoRepository.count();
    }
}