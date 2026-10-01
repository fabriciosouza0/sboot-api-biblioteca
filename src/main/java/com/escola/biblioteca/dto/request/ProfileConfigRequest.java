package com.escola.biblioteca.dto.request;

public record ProfileConfigRequest(
        String profile,
        int maxLoans,
        int loanDays,
        int maxRenewals,
        int holdLimit,
        int fineRateCents,
        int fineCapCents
) {}
