package com.escola.biblioteca.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.dto.request.AutorRequest;
import com.escola.biblioteca.dto.response.AutorResponse;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Autor;
import com.escola.biblioteca.repository.AutorRepository;
import com.escola.biblioteca.repository.query.AutorQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutorService {

    private final AutorRepository autorRepository;
    private final AutorQueryRepository autorQueryRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<AutorResponse> listar(String nome) {
        String term = (nome == null || nome.isBlank()) ? null : nome.trim();
        return autorQueryRepository.buscarTodos(term);
    }

    @Transactional
    public AutorResponse salvar(AutorRequest request) {
        AutorResponse response = toResponse(autorRepository.save(fromRequest(request)));
        dashboardPublisher.dadosAlterados();
        return response;
    }

    @Transactional
    public AutorResponse atualizar(Integer id, AutorRequest request) {
        if (!autorRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.autor");
        }
        Autor autor = fromRequest(request);
        autor.setCodigo(id);
        AutorResponse response = toResponse(autorRepository.save(autor));
        dashboardPublisher.dadosAlterados();
        return response;
    }

    @Transactional
    public void remover(Integer id) {
        if (!autorRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.autor");
        }
        autorRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
    }

    public long count() {
        return autorRepository.count();
    }

    private Autor fromRequest(AutorRequest request) {
        Autor autor = new Autor();
        autor.setNome(request.nome());
        return autor;
    }

    private AutorResponse toResponse(Autor autor) {
        return new AutorResponse(autor.getCodigo(), autor.getNome());
    }
}