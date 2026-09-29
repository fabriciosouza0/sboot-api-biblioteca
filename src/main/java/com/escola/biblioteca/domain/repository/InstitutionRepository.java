package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.Institution;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends CrudRepository<Institution, UUID> {
    Optional<Institution> findByCode(String code);

    @Query("SELECT i.* FROM institution i " +
           "JOIN adm_institution ai ON ai.institution_id = i.id " +
           "JOIN adms a ON a.codigo = ai.adm_codigo " +
           "WHERE a.login = :login")
    List<Institution> findInstitutionsByAdmLogin(@Param("login") String login);
}