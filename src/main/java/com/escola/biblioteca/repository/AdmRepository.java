package com.escola.biblioteca.repository;

import com.escola.biblioteca.model.Adm;
import com.escola.biblioteca.model.AdminRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AdmRepository extends CrudRepository<Adm, Integer> {

    Optional<Adm> findByLogin(String login);

    List<Adm> findByRoleAndInstitutionId(AdminRole role, UUID institutionId);

    List<Adm> findByInstitutionId(UUID institutionId);

    boolean existsByLogin(String login);

    @Query("SELECT ai.institution_id FROM adm_institution ai WHERE ai.adm_codigo = :codigo LIMIT 1")
    Optional<UUID> findFirstInstitutionIdByAdmCodigo(@Param("codigo") Integer codigo);
}