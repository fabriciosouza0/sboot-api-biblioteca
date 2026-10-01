package com.escola.biblioteca.domain.patron.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.patron.model.enums.PatronProfile;
import com.escola.biblioteca.domain.patron.model.ProfileConfig;
import com.escola.biblioteca.domain.patron.repository.ProfileConfigRepository;
import com.escola.biblioteca.dto.request.ProfileConfigRequest;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileConfigService {

    private final ProfileConfigRepository profileConfigRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<ProfileConfig> findByInstitutionId(UUID institutionId) {
        var result = new ArrayList<ProfileConfig>();
        profileConfigRepository.findAll().forEach(pc -> {
            if (pc.getInstitutionId().equals(institutionId)) {
                result.add(pc);
            }
        });
        return result;
    }

    public ProfileConfig findById(UUID id) {
        return profileConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.profile_config"));
    }

    @Transactional
    public ProfileConfig create(UUID institutionId, ProfileConfigRequest request) {
        ProfileConfig config = new ProfileConfig();
        config.setInstitutionId(institutionId);
        config.setProfile(PatronProfile.valueOf(request.profile()));
        config.setMaxLoans(request.maxLoans());
        config.setLoanDays(request.loanDays());
        config.setMaxRenewals(request.maxRenewals());
        config.setHoldLimit(request.holdLimit());
        config.setFineRateCents(request.fineRateCents());
        config.setFineCapCents(request.fineCapCents());
        config.marcarNovo();

        ProfileConfig saved = profileConfigRepository.save(config);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public ProfileConfig update(UUID id, ProfileConfigRequest request) {
        ProfileConfig config = findById(id);
        config.setProfile(PatronProfile.valueOf(request.profile()));
        config.setMaxLoans(request.maxLoans());
        config.setLoanDays(request.loanDays());
        config.setMaxRenewals(request.maxRenewals());
        config.setHoldLimit(request.holdLimit());
        config.setFineRateCents(request.fineRateCents());
        config.setFineCapCents(request.fineCapCents());

        ProfileConfig saved = profileConfigRepository.save(config);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        profileConfigRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
    }
}
