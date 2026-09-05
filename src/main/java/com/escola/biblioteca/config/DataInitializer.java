package com.escola.biblioteca.config;

import com.escola.biblioteca.domain.model.Institution;
import com.escola.biblioteca.domain.model.ProfileConfig;
import com.escola.biblioteca.domain.repository.InstitutionRepository;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String DEFAULT_CODE = "EEEP-JBL";
    private static final String DEFAULT_NAME = "E.E.E.P. Pe. João Bosco de Lima";

    private final InstitutionRepository institutionRepository;
    private final ProfileConfigRepository profileConfigRepository;

    public DataInitializer(InstitutionRepository institutionRepository,
                           ProfileConfigRepository profileConfigRepository) {
        this.institutionRepository = institutionRepository;
        this.profileConfigRepository = profileConfigRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultInstitution() {
        Institution institution = institutionRepository.findByCode(DEFAULT_CODE)
                .orElseGet(() -> {
                    log.info("Criando instituição padrão: {} ({})", DEFAULT_NAME, DEFAULT_CODE);
                    Institution inst = new Institution();
                    inst.setCode(DEFAULT_CODE);
                    inst.setName(DEFAULT_NAME);
                    inst.setCreatedAt(Instant.now());
                    inst.marcarNovo();
                    return institutionRepository.save(inst);
                });

        seedProfileConfigs(institution);
    }

    private void seedProfileConfigs(Institution institution) {
        record ProfileDefaults(String profile, int maxLoans, int loanDays,
                               int maxRenewals, int holdLimit, int fineRateCents, int fineCapCents) {}

        var profiles = java.util.List.of(
                new ProfileDefaults("STUDENT", 3, 7, 2, 5, 50, 5000),
                new ProfileDefaults("TEACHER", 10, 30, 3, 10, 50, 5000),
                new ProfileDefaults("STAFF", 999, 60, 999, 999, 0, 0),
                new ProfileDefaults("EXTERNAL", 2, 14, 1, 3, 100, 10000)
        );

        for (ProfileDefaults p : profiles) {
            profileConfigRepository.findByInstitutionIdAndProfile(institution.getId(),
                    com.escola.biblioteca.domain.model.PatronProfile.valueOf(p.profile()))
                    .orElseGet(() -> {
                        log.info("Criando profile_config: {} para {}", p.profile(), DEFAULT_CODE);
                        ProfileConfig config = new ProfileConfig();
                        config.setInstitutionId(institution.getId());
                        config.setProfile(com.escola.biblioteca.domain.model.PatronProfile.valueOf(p.profile()));
                        config.setMaxLoans(p.maxLoans());
                        config.setLoanDays(p.loanDays());
                        config.setMaxRenewals(p.maxRenewals());
                        config.setHoldLimit(p.holdLimit());
                        config.setFineRateCents(p.fineRateCents());
                        config.setFineCapCents(p.fineCapCents());
                        config.setCreatedAt(Instant.now());
                        config.marcarNovo();
                        return profileConfigRepository.save(config);
                    });
        }
    }
}
