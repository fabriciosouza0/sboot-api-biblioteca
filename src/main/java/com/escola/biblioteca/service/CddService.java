package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.CddRequest;
import com.escola.biblioteca.dto.request.CddUpdateRequest;
import com.escola.biblioteca.dto.response.CddResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Cdd;
import com.escola.biblioteca.repository.CddRepository;
import com.escola.biblioteca.repository.query.CddQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CddService {

    private final CddRepository cddRepository;
    private final CddQueryRepository cddQueryRepository;

    public List<CddResponse> listar(String descricao) {
        String term = (descricao == null || descricao.isBlank()) ? null : descricao.trim();
        return cddQueryRepository.buscarTodos(term);
    }

    @Transactional
    public CddResponse salvar(CddRequest request) {
        if (cddRepository.existsById(request.id())) {
            throw new BusinessException("error.cdd.codigo.duplicado");
        }
        Cdd cdd = new Cdd();
        cdd.setCodigo(request.id());
        cdd.setDescricao(request.descricao());
        cdd.marcarNovo();
        return toResponse(cddRepository.save(cdd));
    }

    @Transactional
    public CddResponse atualizar(Long id, CddUpdateRequest request) {
        if (!cddRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.cdd");
        }
        Cdd cdd = new Cdd();
        cdd.setCodigo(id);
        cdd.setDescricao(request.descricao());
        return toResponse(cddRepository.save(cdd));
    }

    @Transactional
    public void remover(Long id) {
        if (!cddRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.cdd");
        }
        cddRepository.deleteById(id);
    }

    public long count() {
        return cddRepository.count();
    }

    private CddResponse toResponse(Cdd cdd) {
        return new CddResponse(cdd.getCodigo(), cdd.getDescricao());
    }
}