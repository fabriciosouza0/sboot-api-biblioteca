package com.escola.biblioteca.job;

import com.escola.biblioteca.domain.circulation.service.FineService;
import com.escola.biblioteca.domain.circulation.service.HoldService;
import com.escola.biblioteca.domain.circulation.service.LoanService;
import com.escola.biblioteca.domain.patron.service.PatronService;
import com.escola.biblioteca.model.enums.AdminRole;
import com.escola.biblioteca.security.AdminAuthenticationToken;
import com.escola.biblioteca.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CirculationJobService {

    private static final Logger log = LoggerFactory.getLogger(CirculationJobService.class);

    private final HoldService holdService;
    private final LoanService loanService;
    private final FineService fineService;
    private final PatronService patronService;
    private final RefreshTokenService refreshTokenService;

    public void initSystemSecurityContext() {
        AdminAuthenticationToken systemAuth = new AdminAuthenticationToken(
                "system-job-runner",
                AdminRole.GLOBAL_ADMIN,
                Collections.emptyList(),
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(systemAuth);
    }

    @Transactional
    public int runHoldExpiry() {
        initSystemSecurityContext();
        log.info("[Job:hold-expiry] Iniciando verificação de reservas expiradas...");
        long start = System.currentTimeMillis();
        int expired = holdService.expireHolds(OffsetDateTime.now());
        log.info("[Job:hold-expiry] Concluído em {}ms. Reservas expiradas/promovidas: {}", (System.currentTimeMillis() - start), expired);
        return expired;
    }

    @Transactional
    public int runOverdueDetection() {
        initSystemSecurityContext();
        log.info("[Job:overdue-detection] Iniciando detecção de empréstimos em atraso...");
        long start = System.currentTimeMillis();
        int overdues = loanService.detectAndMarkOverdues(OffsetDateTime.now());
        log.info("[Job:overdue-detection] Concluído em {}ms. Empréstimos marcados como OVERDUE: {}", (System.currentTimeMillis() - start), overdues);
        return overdues;
    }

    @Transactional
    public int runFineAccrual() {
        initSystemSecurityContext();
        log.info("[Job:fine-accrual] Iniciando apuração/acumulação diária de multas por atraso...");
        long start = System.currentTimeMillis();
        int fines = fineService.accrueOverdueFines(OffsetDateTime.now());
        log.info("[Job:fine-accrual] Concluído em {}ms. Multas geradas/atualizadas: {}", (System.currentTimeMillis() - start), fines);
        return fines;
    }

    @Transactional
    public int runAutoRenewal() {
        initSystemSecurityContext();
        log.info("[Job:auto-renewal] Iniciando renovação automática de empréstimos elegíveis...");
        long start = System.currentTimeMillis();
        int renewed = loanService.autoRenewEligibleLoans(OffsetDateTime.now(), 3);
        log.info("[Job:auto-renewal] Concluído em {}ms. Empréstimos auto-renovados: {}", (System.currentTimeMillis() - start), renewed);
        return renewed;
    }

    @Transactional
    public int runPatronBlockCheck() {
        initSystemSecurityContext();
        log.info("[Job:patron-block] Iniciando verificação de bloqueio de leitores por débitos...");
        long start = System.currentTimeMillis();
        int blocked = patronService.updatePatronBlockStatuses();
        log.info("[Job:patron-block] Concluído em {}ms. Leitores bloqueados: {}", (System.currentTimeMillis() - start), blocked);
        return blocked;
    }

    @Transactional
    public void runCleanup() {
        initSystemSecurityContext();
        log.info("[Job:cleanup] Limpando refresh tokens expirados ou revogados...");
        long start = System.currentTimeMillis();
        refreshTokenService.cleanExpiredOrRevoked();
        log.info("[Job:cleanup] Concluído em {}ms.", (System.currentTimeMillis() - start));
    }

    @Transactional
    public MaintenanceReport runDailyMaintenance() {
        initSystemSecurityContext();
        log.info("=================================================================");
        log.info(">>> INICIANDO ROTINA DIÁRIA DE MANUTENÇÃO DA CIRCULAÇÃO <<<");
        log.info("=================================================================");
        long start = System.currentTimeMillis();

        int overdues = runOverdueDetection();
        int fines = runFineAccrual();
        int blocked = runPatronBlockCheck();
        int expiredHolds = runHoldExpiry();
        int renewed = runAutoRenewal();
        runCleanup();

        long totalDuration = System.currentTimeMillis() - start;
        log.info("=================================================================");
        log.info(">>> ROTINA DIÁRIA FINALIZADA COM SUCESSO EM {}ms <<<", totalDuration);
        log.info("Resumo: Overdues={}, Fines={}, Blocked={}, ExpiredHolds={}, Renewed={}",
                overdues, fines, blocked, expiredHolds, renewed);
        log.info("=================================================================");

        return new MaintenanceReport(overdues, fines, blocked, expiredHolds, renewed, totalDuration);
    }

    public record MaintenanceReport(
            int overduesMarked,
            int finesProcessed,
            int patronsBlocked,
            int holdsExpired,
            int loansRenewed,
            long durationMs
    ) {}
}
