package com.escola.biblioteca.domain.patron.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.patron.model.Patron;
import com.escola.biblioteca.domain.patron.model.enums.PatronProfile;
import com.escola.biblioteca.domain.patron.model.enums.PatronStatus;
import com.escola.biblioteca.domain.patron.repository.PatronRepository;
import com.escola.biblioteca.domain.patron.repository.ProfileConfigRepository;
import com.escola.biblioteca.domain.patron.repository.PatronQueryRepository;
import com.escola.biblioteca.domain.circulation.policy.LoanPolicy;
import com.escola.biblioteca.dto.request.PatronRequest;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatronService {

    private final PatronRepository patronRepository;
    private final PatronQueryRepository patronQueryRepository;
    private final ProfileConfigRepository profileConfigRepository;
    private final DashboardPublisher dashboardPublisher;

    public List<Patron> search(UUID institutionId, String name) {
        if (name != null && !name.isBlank()) {
            return patronQueryRepository.searchByName(institutionId, name);
        }
        return patronRepository.findByInstitutionId(institutionId);
    }

    @Transactional
    public Patron create(UUID institutionId, PatronRequest request) {
        return register(institutionId, request.externalId(), request.name(), request.phone(),
                PatronProfile.valueOf(request.profile()));
    }

    @Transactional
    public Patron register(UUID institutionId, String externalId, String name, String phone, PatronProfile profile) {
        if (patronRepository.findByInstitutionIdAndExternalId(institutionId, externalId).isPresent()) {
            throw new BusinessException("error.patron.duplicate.externalId", externalId);
        }
        Patron patron = new Patron();
        patron.setInstitutionId(institutionId);
        patron.setExternalId(externalId);
        patron.setName(name);
        patron.setPhone(phone);
        patron.setProfile(profile);
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(0);
        patron.marcarNovo();
        Patron saved = patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Patron update(UUID id, PatronRequest request) {
        Patron patron = findById(id);
        patron.setExternalId(request.externalId());
        patron.setName(request.name());
        patron.setPhone(request.phone());
        patron.setProfile(PatronProfile.valueOf(request.profile()));
        Patron saved = patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        patronRepository.deleteById(id);
        dashboardPublisher.dadosAlterados();
    }

    public Patron findById(UUID patronId) {
        return patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.patron"));
    }

    public Patron findByInstitutionIdAndExternalId(UUID institutionId, String externalId) {
        return patronRepository.findByInstitutionIdAndExternalId(institutionId, externalId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.patron"));
    }

    @Transactional
    public Patron updateProfile(UUID patronId, PatronProfile newProfile) {
        Patron patron = findById(patronId);
        patron.setProfile(newProfile);
        Patron saved = patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public void block(UUID patronId) {
        Patron patron = findById(patronId);
        patron.setStatus(PatronStatus.BLOCKED);
        patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
    }

    @Transactional
    public void unblock(UUID patronId) {
        Patron patron = findById(patronId);
        patron.setStatus(PatronStatus.ACTIVE);
        patronRepository.save(patron);
        dashboardPublisher.dadosAlterados();
    }

    public LoanPolicy getLimits(UUID institutionId, PatronProfile profile) {
        return LoanPolicy.fromProfileConfig(profileConfigRepository, new Patron() {{
            setInstitutionId(institutionId);
            setProfile(profile);
        }});
    }

    public LoanPolicy getLimits(Patron patron) {
        return LoanPolicy.fromProfileConfig(profileConfigRepository, patron);
    }

    public List<Patron> findByInstitutionId(UUID institutionId) {
        return patronRepository.findByInstitutionId(institutionId);
    }

    public boolean isBlocked(Patron patron) {
        return patron.getStatus() == PatronStatus.BLOCKED || patron.getFineBalance() > 2000;
    }
}