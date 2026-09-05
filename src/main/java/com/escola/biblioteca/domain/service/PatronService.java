package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.model.PatronStatus;
import com.escola.biblioteca.domain.repository.PatronRepository;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
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
    private final ProfileConfigRepository profileConfigRepository;

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
        return patronRepository.save(patron);
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
        return patronRepository.save(patron);
    }

    @Transactional
    public void block(UUID patronId) {
        Patron patron = findById(patronId);
        patron.setStatus(PatronStatus.BLOCKED);
        patronRepository.save(patron);
    }

    @Transactional
    public void unblock(UUID patronId) {
        Patron patron = findById(patronId);
        patron.setStatus(PatronStatus.ACTIVE);
        patronRepository.save(patron);
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