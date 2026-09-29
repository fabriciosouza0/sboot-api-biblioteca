package com.escola.biblioteca.service;

import com.escola.biblioteca.domain.repository.FineRepository;
import com.escola.biblioteca.domain.repository.HoldRepository;
import com.escola.biblioteca.domain.repository.InstitutionRepository;
import com.escola.biblioteca.domain.repository.ItemRepository;
import com.escola.biblioteca.domain.repository.LibraryRepository;
import com.escola.biblioteca.domain.repository.LoanRepository;
import com.escola.biblioteca.domain.repository.PatronRepository;
import com.escola.biblioteca.domain.repository.WorkRepository;
import com.escola.biblioteca.domain.model.HoldStatus;
import com.escola.biblioteca.domain.model.FineStatus;
import com.escola.biblioteca.domain.model.ItemStatus;
import com.escola.biblioteca.domain.model.LoanStatus;
import com.escola.biblioteca.domain.model.PatronStatus;
import com.escola.biblioteca.dto.response.DashboardResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final InstitutionRepository institutionRepository;

    public DashboardResponse stats() {
        return stats(null);
    }

    public DashboardResponse stats(UUID institutionId) {
        long totalWorks = countWorks(institutionId);
        long totalItems = countItems(institutionId);
        long availableItems = countAvailableItems(institutionId);
        long totalPatrons = countPatrons(institutionId);
        long activePatrons = countActivePatrons(institutionId);
        long activeLoans = countActiveLoans(institutionId);
        long overdueLoans = countOverdueLoans(institutionId);
        long readyHolds = countReadyHolds(institutionId);
        long pendingFines = countPendingFines(institutionId);
        long totalFineBalanceCents = sumFineBalance(institutionId);
        String totalFineBalance = formatCurrency(totalFineBalanceCents);
        long totalLibraries = countLibraries(institutionId);
        long inTransitItems = countItemsByStatus(institutionId, ItemStatus.IN_TRANSIT);
        long inRepairItems = countItemsByStatus(institutionId, ItemStatus.IN_REPAIR);

        return new DashboardResponse(
                totalWorks, totalItems, availableItems,
                totalPatrons, activePatrons,
                activeLoans, overdueLoans,
                readyHolds, pendingFines,
                totalFineBalance, totalFineBalanceCents,
                totalLibraries, inTransitItems, inRepairItems);
    }

    private long countWorks(UUID institutionId) {
        if (institutionId != null) {
            return workRepository.findByInstitutionId(institutionId).size();
        }
        var all = new ArrayList<UUID>();
        workRepository.findAll().forEach(w -> all.add(w.getId()));
        return all.size();
    }

    private long countItems(UUID institutionId) {
        var all = new ArrayList<Object>();
        itemRepository.findAll().forEach(all::add);
        return all.size();
    }

    private long countAvailableItems(UUID institutionId) {
        return countItemsByStatus(institutionId, ItemStatus.AVAILABLE);
    }

    private long countItemsByStatus(UUID institutionId, ItemStatus status) {
        var all = new ArrayList<Object>();
        itemRepository.findAll().forEach(i -> {
            if (((com.escola.biblioteca.domain.model.Item) i).getStatus() == status) {
                all.add(i);
            }
        });
        return all.size();
    }

    private long countPatrons(UUID institutionId) {
        if (institutionId != null) {
            return patronRepository.findByInstitutionId(institutionId).size();
        }
        var all = new ArrayList<UUID>();
        patronRepository.findAll().forEach(p -> all.add(p.getId()));
        return all.size();
    }

    private long countActivePatrons(UUID institutionId) {
        var all = new ArrayList<com.escola.biblioteca.domain.model.Patron>();
        patronRepository.findAll().forEach(all::add);
        return all.stream().filter(p -> p.getStatus() == PatronStatus.ACTIVE).count();
    }

    private long countActiveLoans(UUID institutionId) {
        var all = new ArrayList<com.escola.biblioteca.domain.model.Loan>();
        loanRepository.findAll().forEach(all::add);
        return all.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
    }

    private long countOverdueLoans(UUID institutionId) {
        var all = new ArrayList<com.escola.biblioteca.domain.model.Loan>();
        loanRepository.findAll().forEach(all::add);
        return all.stream().filter(l -> l.getStatus() == LoanStatus.OVERDUE).count();
    }

    private long countReadyHolds(UUID institutionId) {
        var all = new ArrayList<com.escola.biblioteca.domain.model.Hold>();
        holdRepository.findAll().forEach(all::add);
        return all.stream().filter(h -> h.getStatus() == HoldStatus.READY).count();
    }

    private long countPendingFines(UUID institutionId) {
        var all = new ArrayList<com.escola.biblioteca.domain.model.Fine>();
        fineRepository.findAll().forEach(all::add);
        return all.stream().filter(f -> f.getStatus() == FineStatus.PENDING || f.getStatus() == FineStatus.PARTIAL).count();
    }

    private long sumFineBalance(UUID institutionId) {
        var all = new ArrayList<com.escola.biblioteca.domain.model.Fine>();
        fineRepository.findAll().forEach(all::add);
        return all.stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING || f.getStatus() == FineStatus.PARTIAL)
                .mapToLong(com.escola.biblioteca.domain.model.Fine::getBalanceCents)
                .sum();
    }

    private long countLibraries(UUID institutionId) {
        if (institutionId != null) {
            return libraryRepository.findByInstitutionId(institutionId).size();
        }
        var all = new ArrayList<UUID>();
        libraryRepository.findAll().forEach(l -> all.add(l.getId()));
        return all.size();
    }

    private String formatCurrency(long cents) {
        BigDecimal value = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return "R$ " + value.toString().replace('.', ',');
    }
}
