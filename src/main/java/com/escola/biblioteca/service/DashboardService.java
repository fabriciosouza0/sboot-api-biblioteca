package com.escola.biblioteca.service;

import com.escola.biblioteca.domain.circulation.repository.FineRepository;
import com.escola.biblioteca.domain.circulation.repository.HoldRepository;
import com.escola.biblioteca.domain.institution.repository.InstitutionRepository;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.domain.catalog.repository.LibraryRepository;
import com.escola.biblioteca.domain.circulation.repository.LoanRepository;
import com.escola.biblioteca.domain.patron.repository.PatronRepository;
import com.escola.biblioteca.domain.catalog.repository.WorkRepository;
import com.escola.biblioteca.dto.response.DashboardResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WorkRepository workRepository;
    private final ItemRepository itemRepository;
    private final PatronRepository patronRepository;
    private final LoanRepository loanRepository;
    private final HoldRepository holdRepository;
    private final FineRepository fineRepository;
    private final LibraryRepository libraryRepository;

    @Transactional(readOnly = true)
    public DashboardResponse stats(UUID institutionId) {
        long totalWorks, totalItems, availableItems, totalPatrons, activePatrons;
        long activeLoans, overdueLoans, readyHolds, pendingFines, totalFineBalanceCents;
        long totalLibraries, inTransitItems, inRepairItems;

        if (institutionId != null) {
            totalWorks = workRepository.countByInstitutionId(institutionId);
            totalItems = itemRepository.countByInstitutionId(institutionId);
            availableItems = itemRepository.countByInstitutionIdAndStatus(institutionId, "AVAILABLE");
            inTransitItems = itemRepository.countByInstitutionIdAndStatus(institutionId, "IN_TRANSIT");
            inRepairItems = itemRepository.countByInstitutionIdAndStatus(institutionId, "IN_REPAIR");
            totalPatrons = patronRepository.countByInstitutionId(institutionId);
            activePatrons = patronRepository.countByInstitutionIdAndStatus(institutionId, "ACTIVE");
            activeLoans = loanRepository.countByInstitutionIdAndStatus(institutionId, "ACTIVE");
            overdueLoans = loanRepository.countByInstitutionIdAndStatus(institutionId, "OVERDUE");
            readyHolds = holdRepository.countByInstitutionIdAndStatus(institutionId, "READY");
            pendingFines = fineRepository.countPendingByInstitutionId(institutionId);
            totalFineBalanceCents = fineRepository.sumBalanceByInstitutionId(institutionId);
            totalLibraries = libraryRepository.countByInstitutionId(institutionId);
        } else {
            totalWorks = workRepository.countAll();
            totalItems = itemRepository.countAll();
            availableItems = itemRepository.countAllByStatus("AVAILABLE");
            inTransitItems = itemRepository.countAllByStatus("IN_TRANSIT");
            inRepairItems = itemRepository.countAllByStatus("IN_REPAIR");
            totalPatrons = patronRepository.countAll();
            activePatrons = patronRepository.countAllByStatus("ACTIVE");
            activeLoans = loanRepository.countAllByStatus("ACTIVE");
            overdueLoans = loanRepository.countAllByStatus("OVERDUE");
            readyHolds = holdRepository.countAllByStatus("READY");
            pendingFines = fineRepository.countAllPending();
            totalFineBalanceCents = fineRepository.sumAllBalance();
            totalLibraries = libraryRepository.countAll();
        }

        String totalFineBalance = formatCurrency(totalFineBalanceCents);

        return new DashboardResponse(
                totalWorks, totalItems, availableItems,
                totalPatrons, activePatrons,
                activeLoans, overdueLoans,
                readyHolds, pendingFines,
                totalFineBalance, totalFineBalanceCents,
                totalLibraries, inTransitItems, inRepairItems);
    }

    private String formatCurrency(long cents) {
        BigDecimal value = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return "R$ " + value.toString().replace('.', ',');
    }
}
