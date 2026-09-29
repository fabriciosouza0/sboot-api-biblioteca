package com.escola.biblioteca.config;

import com.escola.biblioteca.domain.model.Institution;
import com.escola.biblioteca.domain.model.ProfileConfig;
import com.escola.biblioteca.domain.repository.InstitutionRepository;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import com.escola.biblioteca.model.AdminRole;
import com.escola.biblioteca.model.Adm;
import com.escola.biblioteca.repository.AdmRepository;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String DEFAULT_CODE = "EEEP-JBL";
    private static final String DEFAULT_NAME = "E.E.E.P. Pe. João Bosco de Lima";
    private static final String GLOBAL_ADMIN_LOGIN = "global-admin";
    private static final String GLOBAL_ADMIN_PASSWORD = "Admin@123";

    private final InstitutionRepository institutionRepository;
    private final ProfileConfigRepository profileConfigRepository;
    private final AdmRepository admRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(InstitutionRepository institutionRepository,
                           ProfileConfigRepository profileConfigRepository,
                           AdmRepository admRepository,
                           PasswordEncoder passwordEncoder) {
        this.institutionRepository = institutionRepository;
        this.profileConfigRepository = profileConfigRepository;
        this.admRepository = admRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultInstitution() {
        Institution institution = institutionRepository.findByCode(DEFAULT_CODE)
                .orElseGet(() -> {
                    log.info("Criando instituição padrão: {} ({})", DEFAULT_NAME, DEFAULT_CODE);
                    Institution inst = new Institution();
                    inst.setCode(DEFAULT_CODE);
                    inst.setName(DEFAULT_NAME);
                    inst.setCreatedAt(OffsetDateTime.now());
                    inst.marcarNovo();
                    return institutionRepository.save(inst);
                });

        seedProfileConfigs(institution);
        seedGlobalAdmin();
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
                        config.setCreatedAt(OffsetDateTime.now());
                        config.marcarNovo();
                        return profileConfigRepository.save(config);
                    });
        }
    }

    private void seedGlobalAdmin() {
        if (admRepository.findByLogin(GLOBAL_ADMIN_LOGIN).isEmpty()) {
            log.info("Criando admin global: {}", GLOBAL_ADMIN_LOGIN);
            Adm adm = new Adm();
            adm.setLogin(GLOBAL_ADMIN_LOGIN);
            adm.setSenha(passwordEncoder.encode(GLOBAL_ADMIN_PASSWORD));
            adm.setNome("Global Admin");
            adm.setRole(AdminRole.GLOBAL_ADMIN);
            adm.setInstitutionId(null);
            admRepository.save(adm);
        }
    }
}
