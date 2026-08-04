package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Cdd;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CddRepository extends JpaRepository<Cdd, Long> {

    List<Cdd> findByDescricaoContainingIgnoreCase(String descricao);
}
