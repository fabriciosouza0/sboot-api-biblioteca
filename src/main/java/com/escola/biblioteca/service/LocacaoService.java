package com.escola.biblioteca.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.dto.request.LocacaoRequest;
import com.escola.biblioteca.dto.response.LocacaoResponse;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import com.escola.biblioteca.model.Livro;
import com.escola.biblioteca.model.Loca;
import com.escola.biblioteca.model.Locatario;
import com.escola.biblioteca.repository.LivroRepository;
import com.escola.biblioteca.repository.LocaRepository;
import com.escola.biblioteca.repository.LocatarioRepository;
import com.escola.biblioteca.repository.query.LocacaoQueryRepository;
import com.escola.biblioteca.repository.query.LocacaoQueryRepository.LocacaoRow;
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
    private final LocacaoQueryRepository locacaoQueryRepository;
    private final DashboardPublisher dashboardPublisher;

    @Transactional
    public LocacaoResponse salvar(LocacaoRequest request) {
        Livro livro = livroRepository.findById(request.codigoLivro())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.livro"));
        if (livro.getQtd() == null || livro.getQtd() <= 0) {
            throw new BusinessException("error.livro.sem.exemplares");
        }
        Locatario locatario = locatarioRepository.findById(request.cpfLocatario())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.locatario"));

        LocalDate hoje = LocalDate.now();
        Loca loca = new Loca();
        loca.setCodigoLivro(livro.getCodigo());
        loca.setCpfLocatario(locatario.getCpf());
        loca.setDataDeLocacao(hoje);
        loca.setDataParaDevolucao(hoje.plusDays(request.dias()));
        loca.setAtrasado(false);

        dashboardPublisher.dadosAlterados();
        Loca salvo = locaRepository.save(loca);
        return toResponse(new LocacaoRow(
                salvo.getCodigo(),
                salvo.getCodigoLivro(),
                livro.getTitulo(),
                salvo.getCpfLocatario(),
                locatario.getNome(),
                salvo.getDataDeLocacao(),
                salvo.getDataParaDevolucao()));
    }

    public List<LocacaoResponse> listar(String locatario) {
        String term = (locatario == null || locatario.isBlank()) ? null : locatario.trim();
        return locacaoQueryRepository.buscarTodas(term).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void devolver(Integer id) {
        if (!locaRepository.existsById(id)) {
            throw new ResourceNotFoundException("error.notfound.locacao");
        }
        locaRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
    }

    private LocacaoResponse toResponse(LocacaoRow row) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), row.dataParaDevolucao());
        boolean atrasado = dias <= 0;
        return new LocacaoResponse(
                row.codigo(),
                row.codigoLivro(),
                row.livro(),
                row.cpfLocatario(),
                row.locatario(),
                row.dataDeLocacao(),
                row.dataParaDevolucao(),
                atrasado,
                dias);
    }
}