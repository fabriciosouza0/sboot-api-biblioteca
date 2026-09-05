package com.escola.biblioteca.domain.repository;

import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.model.ProfileConfig;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProfileConfigRepository extends CrudRepository<ProfileConfig, UUID> {
    Optional<ProfileConfig> findByInstitutionIdAndProfile(UUID institutionId, PatronProfile profile);
}