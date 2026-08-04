package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.LivroRequest;
import com.escola.biblioteca.dto.response.LivroResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.mapper.LivroMapper;
import com.escola.biblioteca.model.Autor;
import com.escola.biblioteca.model.Cdd;
import com.escola.biblioteca.model.Livro;
import com.escola.biblioteca.repository.AutorRepository;
import com.escola.biblioteca.repository.CddRepository;
import com.escola.biblioteca.repository.LivroRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final CddRepository cddRepository;
    private final LivroMapper livroMapper;

    public List<LivroResponse> listar(String titulo) {
        List<Livro> livros = (titulo == null || titulo.isBlank())
                ? livroRepository.findAllByOrderByTituloAsc()
                : livroRepository.findByTituloContainingIgnoreCase(titulo.trim());
        return livros.stream().map(livroMapper::toResponse).toList();
    }

    @Transactional
    public LivroResponse salvar(LivroRequest request) {
        if (livroRepository.existsById(request.codigo())) {
            throw new BusinessException("error.livro.codigo.duplicado");
        }
        Autor autor = buscarAutor(request.codigoAutor());
        Cdd cdd = buscarCdd(request.codigoCDD());
        Livro livro = livroMapper.toEntity(request, autor, cdd);
        return livroMapper.toResponse(livroRepository.save(livro));
    }

    @Transactional
    public LivroResponse atualizar(Long codigo, LivroRequest request) {
        Livro livro = livroRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.livro"));
        Autor autor = buscarAutor(request.codigoAutor());
        Cdd cdd = buscarCdd(request.codigoCDD());
        livroMapper.updateEntity(request, autor, cdd, livro);
        return livroMapper.toResponse(livroRepository.save(livro));
    }

    @Transactional
    public void remover(Long codigo) {
        if (!livroRepository.existsById(codigo)) {
            throw new ResourceNotFoundException("error.notfound.livro");
        }
        livroRepository.deleteById(codigo);
    }

    public long count() {
        return livroRepository.count();
    }

    private Autor buscarAutor(Integer codigo) {
        return autorRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.autor"));
    }

    private Cdd buscarCdd(Long codigo) {
        return cddRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.cdd"));
    }
}