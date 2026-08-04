package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.CddRequest;
import com.escola.biblioteca.dto.request.CddUpdateRequest;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Cdd;
import com.escola.biblioteca.repository.CddRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CddService {

    private final CddRepository cddRepository;

    public List<Cdd> listar(String descricao) {
        return (descricao == null || descricao.isBlank())
                ? cddRepository.findAll()
                : cddRepository.findByDescricaoContainingIgnoreCase(descricao.trim());
    }

    @Transactional
    public Cdd salvar(CddRequest request) {
        if (cddRepository.existsById(request.id())) {
            throw new BusinessException("error.cdd.codigo.duplicado");
        }
        Cdd cdd = new Cdd();
        cdd.setId(request.id());
        cdd.setDescricao(request.descricao());
        return cddRepository.save(cdd);
    }

    @Transactional
    public Cdd atualizar(Long id, CddUpdateRequest request) {
        if (!cddRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.cdd");
        }
        Cdd cdd = new Cdd();
        cdd.setId(id);
        cdd.setDescricao(request.descricao());
        return cddRepository.save(cdd);
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
}
