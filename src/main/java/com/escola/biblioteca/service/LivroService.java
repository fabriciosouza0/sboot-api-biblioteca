package com.escola.biblioteca.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.dto.request.LivroRequest;
import com.escola.biblioteca.dto.response.LivroResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Livro;
import com.escola.biblioteca.repository.AutorRepository;
import com.escola.biblioteca.repository.CddRepository;
import com.escola.biblioteca.repository.LivroRepository;
import com.escola.biblioteca.repository.query.LivroQueryRepository;
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
    private final LivroQueryRepository livroQueryRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<LivroResponse> listar(String titulo) {
        return livroQueryRepository.buscarTodos(titulo);
    }

    @Transactional
    public LivroResponse salvar(LivroRequest request) {
        if (livroRepository.existsById(request.codigo())) {
            throw new BusinessException("error.livro.codigo.duplicado");
        }
        Integer codigoAutor = buscarAutor(request.codigoAutor());
        Long codigoCdd = buscarCdd(request.codigoCDD());
        Livro livro = new Livro();
        livro.setCodigo(request.codigo());
        livro.setTitulo(request.titulo());
        livro.setQtd(request.qtd());
        livro.setCodigoAutor(codigoAutor);
        livro.setCodigoCdd(codigoCdd);
        livro.marcarNovo();
        livroRepository.save(livro);
        dashboardPublisher.dadosAlterados();
        return livroQueryRepository.buscarPorCodigo(request.codigo())
                .orElseThrow(() -> new IllegalStateException("Livro não encontrado após salvar"));
    }

    @Transactional
    public LivroResponse atualizar(Long codigo, LivroRequest request) {
        Livro livro = livroRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.livro"));
        Integer codigoAutor = buscarAutor(request.codigoAutor());
        Long codigoCdd = buscarCdd(request.codigoCDD());
        livro.setTitulo(request.titulo());
        livro.setQtd(request.qtd());
        livro.setCodigoAutor(codigoAutor);
        livro.setCodigoCdd(codigoCdd);
        livroRepository.save(livro);
        dashboardPublisher.dadosAlterados();
        return livroQueryRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException("Livro não encontrado após atualizar"));
    }

    @Transactional
    public void remover(Long codigo) {
        if (!livroRepository.existsById(codigo)) {
            throw new ResourceNotFoundException("error.notfound.livro");
        }
        livroRepository.deleteById(codigo);
        dashboardPublisher.dadosAlterados();
    }

    private Integer buscarAutor(Integer codigo) {
        return autorRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.autor"))
                .getCodigo();
    }

    private Long buscarCdd(Long codigo) {
        return cddRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.cdd"))
                .getCodigo();
    }
}