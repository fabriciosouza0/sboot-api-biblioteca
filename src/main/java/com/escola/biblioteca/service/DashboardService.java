package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.response.DashboardResponse;
import com.escola.biblioteca.repository.AlunoRepository;
import com.escola.biblioteca.repository.AutorRepository;
import com.escola.biblioteca.repository.CddRepository;
import com.escola.biblioteca.repository.LivroRepository;
import com.escola.biblioteca.repository.LocaRepository;
import com.escola.biblioteca.repository.LocatarioRepository;
import com.escola.biblioteca.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LivroRepository livroRepository;
    private final LocatarioRepository locatarioRepository;
    private final AutorRepository autorRepository;
    private final CddRepository cddRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final LocaRepository locaRepository;

    public DashboardResponse stats() {
        return new DashboardResponse(
                livroRepository.count(),
                locaRepository.countAtrasados(),
                professorRepository.count(),
                alunoRepository.count(),
                locatarioRepository.count(),
                autorRepository.count(),
                cddRepository.count(),
                locaRepository.countProfessoresComLivros(),
                locaRepository.countAlunosComLivros());
    }
}