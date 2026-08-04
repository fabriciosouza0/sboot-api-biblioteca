package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.AutorRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Autor;
import com.escola.biblioteca.repository.AutorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutorService {

    private final AutorRepository autorRepository;

    public List<Autor> listar(String nome) {
        return (nome == null || nome.isBlank())
                ? autorRepository.findAll()
                : autorRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Transactional
    public Autor salvar(AutorRequest request) {
        return autorRepository.save(fromRequest(request));
    }

    @Transactional
    public Autor atualizar(Integer id, AutorRequest request) {
        if (!autorRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.autor");
        }
        Autor autor = fromRequest(request);
        autor.setId(id);
        return autorRepository.save(autor);
    }

    private Autor fromRequest(AutorRequest request) {
        Autor autor = new Autor();
        autor.setNome(request.nome());
        return autor;
    }

    @Transactional
    public void remover(Integer id) {
        if (!autorRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.autor");
        }
        autorRepository.deleteById(id);
    }

    public long count() {
        return autorRepository.count();
    }
}
