package com.escola.biblioteca.domain.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class LoanPolicy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer maxLoans;
    private final Integer loanDays;
    private final Integer maxRenewals;
    private final Integer holdLimit;
    private final Integer fineRateCents;
    private final Integer fineCapCents;

    public static LoanPolicy fromProfileConfig(com.escola.biblioteca.domain.repository.ProfileConfigRepository repo,
                                                com.escola.biblioteca.domain.model.Patron patron) {
        var config = repo.findByInstitutionIdAndProfile(patron.getInstitutionId(), patron.getProfile())
                .orElseThrow(() -> new IllegalStateException(
                        "Perfil %s não configurado para instituição %s".formatted(patron.getProfile(), patron.getInstitutionId())));
        return new LoanPolicy(
                config.getMaxLoans(),
                config.getLoanDays(),
                config.getMaxRenewals(),
                config.getHoldLimit(),
                config.getFineRateCents(),
                config.getFineCapCents()
        );
    }
}