package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.TurmaRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Turma;
import com.escola.biblioteca.repository.TurmaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;

    public List<Turma> listar() {
        return turmaRepository.findAll();
    }

    @Transactional
    public Turma salvar(TurmaRequest request) {
        return turmaRepository.save(fromRequest(request));
    }

    @Transactional
    public Turma atualizar(Integer id, TurmaRequest request) {
        if (!turmaRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.turma");
        }
        Turma turma = fromRequest(request);
        turma.setId(id);
        return turmaRepository.save(turma);
    }

    private Turma fromRequest(TurmaRequest request) {
        Turma turma = new Turma();
        turma.setDescricao(request.descricao());
        return turma;
    }

    @Transactional
    public void remover(Integer id) {
        if (!turmaRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.turma");
        }
        turmaRepository.deleteById(id);
    }
}
