package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.LocacaoRequest;
import com.escola.biblioteca.dto.response.LocacaoResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.mapper.LocacaoMapper;
import com.escola.biblioteca.model.Livro;
import com.escola.biblioteca.model.Loca;
import com.escola.biblioteca.model.Locatario;
import com.escola.biblioteca.repository.LivroRepository;
import com.escola.biblioteca.repository.LocaRepository;
import com.escola.biblioteca.repository.LocatarioRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocacaoService {

    private final LocaRepository locaRepository;
    private final LivroRepository livroRepository;
    private final LocatarioRepository locatarioRepository;
    private final LocacaoMapper locacaoMapper;

    @Transactional
    public LocacaoResponse salvar(LocacaoRequest request) {
        Livro livro = livroRepository.findById(request.codigoLivro())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.livro"));
        if (livro.getQtd() == null || livro.getQtd() <= 0) {
            throw new BusinessException("error.livro.sem.exemplares");
        }
        Locatario locatario = locatarioRepository.findById(request.cpfLocatario())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.locatario"));

        Loca loca = new Loca();
        loca.setLivro(livro);
        loca.setLocatario(locatario);
        loca.setDataDeLocacao(LocalDate.now());
        loca.setDataParaDevolucao(LocalDate.now().plusDays(request.dias()));
        loca.setAtrasado("N");
        return toResponse(locaRepository.save(loca));
    }

    public List<LocacaoResponse> listar(String locatario) {
        List<Loca> locacoes = (locatario == null || locatario.isBlank())
                ? locaRepository.findAllByOrderByDataDeLocacaoDesc()
                : locaRepository.searchPorLocatario(locatario.trim());
        return locacoes.stream().map(this::atualizarStatusEAplicar).toList();
    }

    @Transactional
    public void devolver(Integer id) {
        if (!locaRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.locacao");
        }
        locaRepository.deleteById(id);
    }

    @Transactional
    public long nAtrasados() {
        atualizarFlags();
        return locaRepository.findAll().stream().filter(l -> "Y".equals(l.getAtrasado())).count();
    }

    @Transactional(readOnly = true)
    public long nProfessoresComLivros() {
        return locaRepository.findAll().stream()
                .filter(l -> l.getLocatario().getProfessor() != null)
                .count();
    }

    @Transactional(readOnly = true)
    public long nAlunosComLivros() {
        return locaRepository.findAll().stream()
                .filter(l -> l.getLocatario().getAluno() != null)
                .count();
    }

    private LocacaoResponse atualizarStatusEAplicar(Loca l) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), l.getDataParaDevolucao());
        boolean atrasado = dias <= 0;
        String flag = atrasado ? "Y" : "N";
        if (!flag.equals(l.getAtrasado())) {
            l.setAtrasado(flag);
            locaRepository.save(l);
        }
        return locacaoMapper.toResponse(l, atrasado, dias);
    }

    private void atualizarFlags() {
        for (Loca l : locaRepository.findAll()) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), l.getDataParaDevolucao());
            l.setAtrasado(dias <= 0 ? "Y" : "N");
        }
        locaRepository.flush();
    }

    private LocacaoResponse toResponse(Loca l) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), l.getDataParaDevolucao());
        return locacaoMapper.toResponse(l, dias <= 0, dias);
    }
}