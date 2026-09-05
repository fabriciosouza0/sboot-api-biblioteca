package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.TurmaRequest;
import com.escola.biblioteca.dto.response.TurmaResponse;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Turma;
import com.escola.biblioteca.repository.TurmaRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;

    public List<TurmaResponse> listar() {
        return StreamSupport.stream(turmaRepository.findAll().spliterator(), false)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TurmaResponse salvar(TurmaRequest request) {
        return toResponse(turmaRepository.save(fromRequest(request)));
    }

    @Transactional
    public TurmaResponse atualizar(Integer id, TurmaRequest request) {
        if (!turmaRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.turma");
        }
        Turma turma = fromRequest(request);
        turma.setCodigo(id);
        return toResponse(turmaRepository.save(turma));
    }

    @Transactional
    public void remover(Integer id) {
        if (!turmaRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.turma");
        }
        turmaRepository.deleteById(id);
    }

    private Turma fromRequest(TurmaRequest request) {
        Turma turma = new Turma();
        turma.setDescricao(request.descricao());
        return turma;
    }

    private TurmaResponse toResponse(Turma turma) {
        return new TurmaResponse(turma.getCodigo(), turma.getDescricao());
    }
}