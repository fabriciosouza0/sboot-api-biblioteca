package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Loca;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface LocaRepository extends CrudRepository<Loca, Integer> {

    @Query("SELECT COUNT(*) FROM loca WHERE data_para_devolucao <= CURRENT_DATE")
    long countAtrasados();

    @Query("SELECT COUNT(*) FROM loca l JOIN locatario lt ON lt.cpf = l.cpf_locatario WHERE lt.codigo_professor IS NOT NULL")
    long countProfessoresComLivros();

    @Query("SELECT COUNT(*) FROM loca l JOIN locatario lt ON lt.cpf = l.cpf_locatario WHERE lt.codigo_aluno IS NOT NULL")
    long countAlunosComLivros();
}