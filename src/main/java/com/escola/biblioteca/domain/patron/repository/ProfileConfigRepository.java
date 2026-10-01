package com.escola.biblioteca.domain.patron.repository;

import com.escola.biblioteca.domain.patron.model.enums.PatronProfile;
import com.escola.biblioteca.domain.patron.model.ProfileConfig;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProfileConfigRepository extends CrudRepository<ProfileConfig, UUID> {
    Optional<ProfileConfig> findByInstitutionIdAndProfile(UUID institutionId, PatronProfile profile);
}