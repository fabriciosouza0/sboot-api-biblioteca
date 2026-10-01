package com.escola.biblioteca.domain.circulation.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.catalog.model.Item;
import com.escola.biblioteca.domain.catalog.model.enums.ItemStatus;
import com.escola.biblioteca.domain.circulation.model.Loan;
import com.escola.biblioteca.domain.circulation.model.enums.LoanStatus;
import com.escola.biblioteca.domain.circulation.policy.LoanPolicy;
import com.escola.biblioteca.domain.outbox.model.OutboxEvent;
import com.escola.biblioteca.domain.patron.model.Patron;
import com.escola.biblioteca.domain.patron.service.PatronService;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.domain.circulation.repository.LoanRepository;
import com.escola.biblioteca.domain.outbox.repository.OutboxEventRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final ItemRepository itemRepository;
    private final PatronService patronService;
    private final OutboxEventRepository outboxEventRepository;
    private final DashboardPublisher dashboardPublisher;

    @Transactional
    public Loan checkout(UUID patronId, UUID itemId, UUID libraryId) {
        Patron patron = patronService.findById(patronId);
        if (patronService.isBlocked(patron)) {
            throw new BusinessException("error.patron.blocked");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BusinessException("error.item.not.available");
        }

        LoanPolicy policy = patronService.getLimits(patron);
        long activeLoans = loanRepository.findByPatronIdAndStatusIn(patronId, List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE)).size();
        if (activeLoans >= policy.getMaxLoans()) {
            throw new BusinessException("error.patron.loan.limit.exceeded", policy.getMaxLoans());
        }

        Loan loan = new Loan();
        loan.setPatronId(patronId);
        loan.setItemId(itemId);
        loan.setLibraryId(libraryId);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCheckedOutAt(OffsetDateTime.now());
        loan.setDueAt(OffsetDateTime.now().plus(policy.getLoanDays(), ChronoUnit.DAYS));
        loan.setRenewalCount(0);
        loan.marcarNovo();

        item.setStatus(ItemStatus.ON_LOAN);
        itemRepository.save(item);

        Loan saved = loanRepository.save(loan);
        publishEvent(saved.getId(), "LoanCreated", saved);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Loan returnLoan(UUID loanId, UUID libraryId, String condition) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.loan"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessException("error.loan.already.returned");
        }

        Item item = itemRepository.findById(loan.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.item"));

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(OffsetDateTime.now());
        loan.setReturnedLibraryId(libraryId != null ? libraryId : loan.getLibraryId());

        // Determine item status based on return condition
        if ("DAMAGED".equalsIgnoreCase(condition)) {
            item.setStatus(ItemStatus.IN_REPAIR);
        } else if ("LOST".equalsIgnoreCase(condition)) {
            item.setStatus(ItemStatus.LOST);
        } else {
            item.setStatus(ItemStatus.AVAILABLE);
        }
        itemRepository.save(item);

        Loan saved = loanRepository.save(loan);
        publishEvent(saved.getId(), "LoanReturned", saved);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    @Transactional
    public Loan renew(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.loan"));

        if (loan.getStatus() != LoanStatus.ACTIVE && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new BusinessException("error.loan.not.active");
        }

        Patron patron = patronService.findById(loan.getPatronId());
        if (patronService.isBlocked(patron)) {
            throw new BusinessException("error.patron.blocked");
        }

        LoanPolicy policy = patronService.getLimits(patron);
        if (loan.getRenewalCount() >= policy.getMaxRenewals()) {
            throw new BusinessException("error.loan.renewal.limit.exceeded", policy.getMaxRenewals());
        }

        // Check if there's a waiting hold on this item
        // (HoldService will check this, but we also validate here)
        // TODO: check HoldService for waiting holds on this item's work

        loan.setDueAt(loan.getDueAt().plus(policy.getLoanDays(), ChronoUnit.DAYS));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        if (loan.getStatus() == LoanStatus.OVERDUE) {
            loan.setStatus(LoanStatus.ACTIVE);
        }

        Loan saved = loanRepository.save(loan);
        publishEvent(saved.getId(), "LoanRenewed", saved);
        dashboardPublisher.dadosAlterados();
        return saved;
    }

    public List<Loan> findAll() {
        var all = new ArrayList<Loan>();
        loanRepository.findAll().forEach(all::add);
        return all;
    }

    public List<Loan> findActiveByPatron(UUID patronId) {
        return loanRepository.findByPatronIdAndStatusIn(patronId, List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));
    }

    public List<Loan> findOverdueByPatron(UUID patronId) {
        return loanRepository.findOverdueByPatronId(patronId);
    }

    private void publishEvent(UUID aggregateId, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Loan");
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(com.escola.biblioteca.util.JsonUtil.toJson(payload));
        event.setCreatedAt(java.time.OffsetDateTime.now());
        event.marcarNovo();
        outboxEventRepository.save(event);
    }
}