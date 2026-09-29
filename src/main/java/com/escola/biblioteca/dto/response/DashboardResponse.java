package com.escola.biblioteca.dto.response;

public record DashboardResponse(
        long totalWorks,
        long totalItems,
        long availableItems,
        long totalPatrons,
        long activePatrons,
        long activeLoans,
        long overdueLoans,
        long readyHolds,
        long pendingFines,
        String totalFineBalance,
        long totalFineBalanceCents,
        long totalLibraries,
        long inTransitItems,
        long inRepairItems) {
}
