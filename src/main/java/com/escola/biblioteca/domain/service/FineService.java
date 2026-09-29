package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Fine;
import com.escola.biblioteca.domain.model.FineStatus;
import com.escola.biblioteca.domain.model.FineType;
import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import com.escola.biblioteca.domain.model.OutboxEvent;
import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.repository.FineRepository;
import com.escola.biblioteca.domain.repository.LoanRepository;
import com.escola.biblioteca.domain.repository.OutboxEventRepository;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final LoanRepository loanRepository;
    private final PatronService patronService;
    private final ProfileConfigRepository profileConfigRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public Fine assessOverdue(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.loan"));

        if (loan.getStatus() != LoanStatus.OVERDUE) {
            throw new BusinessException("error.loan.not.overdue");
        }

        Patron patron = patronService.findById(loan.getPatronId());
        var config = profileConfigRepository.findByInstitutionIdAndProfile(patron.getInstitutionId(), patron.getProfile())
                .orElseThrow(() -> new IllegalStateException("Perfil não configurado"));

        long daysOverdue = ChronoUnit.DAYS.between(loan.getDueAt().truncatedTo(ChronoUnit.DAYS), OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
        if (daysOverdue <= 0) {
            throw new BusinessException("error.loan.not.overdue");
        }

        int amountCents = Math.min((int) daysOverdue * config.getFineRateCents(), config.getFineCapCents());

        Fine fine = new Fine();
        fine.setPatronId(patron.getId());
        fine.setLoanId(loanId);
        fine.setType(FineType.OVERDUE);
        fine.setAmountCents(amountCents);
        fine.setBalanceCents(amountCents);
        fine.setStatus(FineStatus.PENDING);
        fine.setAssessedAt(OffsetDateTime.now());
        fine.setReason("Atraso de %d dia(s)".formatted(daysOverdue));
        fine.marcarNovo();

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FineAssessed", saved);
        return saved;
    }

    @Transactional
    public Fine assessLost(UUID loanId, int replacementCostCents) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.loan"));

        Patron patron = patronService.findById(loan.getPatronId());

        Fine fine = new Fine();
        fine.setPatronId(patron.getId());
        fine.setLoanId(loanId);
        fine.setType(FineType.LOST);
        fine.setAmountCents(replacementCostCents);
        fine.setBalanceCents(replacementCostCents);
        fine.setStatus(FineStatus.PENDING);
        fine.setAssessedAt(OffsetDateTime.now());
        fine.setReason("Exemplar perdido");
        fine.marcarNovo();

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FineAssessed", saved);
        return saved;
    }

    @Transactional
    public Fine assessDamage(UUID loanId, int damagePercent) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.loan"));

        Patron patron = patronService.findById(loan.getPatronId());

        Fine fine = new Fine();
        fine.setPatronId(patron.getId());
        fine.setLoanId(loanId);
        fine.setType(FineType.DAMAGE);
        fine.setAmountCents(damagePercent);
        fine.setBalanceCents(damagePercent);
        fine.setStatus(FineStatus.PENDING);
        fine.setAssessedAt(OffsetDateTime.now());
        fine.setReason("Dano ao exemplar (%d%%)".formatted(damagePercent));
        fine.marcarNovo();

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FineAssessed", saved);
        return saved;
    }

    @Transactional
    public Fine assessProcessingFee(UUID patronId, int amountCents, String reason) {
        Patron patron = patronService.findById(patronId);

        Fine fine = new Fine();
        fine.setPatronId(patron.getId());
        fine.setType(FineType.PROCESSING);
        fine.setAmountCents(amountCents);
        fine.setBalanceCents(amountCents);
        fine.setStatus(FineStatus.PENDING);
        fine.setAssessedAt(OffsetDateTime.now());
        fine.setReason(reason);
        fine.marcarNovo();

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FineAssessed", saved);
        return saved;
    }

    @Transactional
    public Fine pay(UUID fineId, String paymentRef, int amountCents) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.fine"));

        if (fine.getStatus() == FineStatus.PAID || fine.getStatus() == FineStatus.WAIVED || fine.getStatus() == FineStatus.WRITTEN_OFF) {
            throw new BusinessException("error.fine.already.resolved");
        }

        if (amountCents <= 0) {
            throw new BusinessException("error.fine.invalid.amount");
        }

        int newBalance = fine.getBalanceCents() - amountCents;
        if (newBalance < 0) {
            throw new BusinessException("error.fine.overpayment");
        }

        fine.setBalanceCents(newBalance);
        if (newBalance == 0) {
            fine.setStatus(FineStatus.PAID);
            fine.setPaidAt(OffsetDateTime.now());
        } else {
            fine.setStatus(FineStatus.PARTIAL);
        }

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FinePaid", saved);
        checkPatronUnblock(fine.getPatronId());
        return saved;
    }

    @Transactional
    public Fine waive(UUID fineId, UUID staffId, String reason) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.fine"));

        if (fine.getStatus() == FineStatus.WAIVED || fine.getStatus() == FineStatus.WRITTEN_OFF || fine.getStatus() == FineStatus.PAID) {
            throw new BusinessException("error.fine.already.resolved");
        }

        fine.setBalanceCents(0);
        fine.setStatus(FineStatus.WAIVED);
        fine.setWaivedAt(OffsetDateTime.now());
        fine.setReason(reason + " (isento por " + staffId + ")");

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FineWaived", saved);
        checkPatronUnblock(fine.getPatronId());
        return saved;
    }

    @Transactional
    public Fine writeOff(UUID fineId, UUID staffId, String reason) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notfound.fine"));

        if (fine.getStatus() == FineStatus.WAIVED || fine.getStatus() == FineStatus.WRITTEN_OFF || fine.getStatus() == FineStatus.PAID) {
            throw new BusinessException("error.fine.already.resolved");
        }

        fine.setBalanceCents(0);
        fine.setStatus(FineStatus.WRITTEN_OFF);
        fine.setWaivedAt(OffsetDateTime.now());
        fine.setReason(reason + " (baixa contábil por " + staffId + ")");

        Fine saved = fineRepository.save(fine);
        publishEvent(saved.getId(), "FineWrittenOff", saved);
        checkPatronUnblock(fine.getPatronId());
        return saved;
    }

    @Transactional
    public void recalculateBalance(UUID patronId) {
        Integer balance = fineRepository.sumBalanceByPatronId(patronId);
        Patron patron = patronService.findById(patronId);
        patron.setFineBalance(balance != null ? balance : 0);
        if (patron.getFineBalance() <= 2000 && patron.getStatus() == com.escola.biblioteca.domain.model.PatronStatus.BLOCKED) {
            patron.setStatus(com.escola.biblioteca.domain.model.PatronStatus.ACTIVE);
        }
        // Note: PatronRepository.save is not exposed here, would need to call via PatronService
    }

    public List<Fine> findPendingByPatron(UUID patronId) {
        return fineRepository.findPendingByPatronId(patronId);
    }

    private void checkPatronUnblock(UUID patronId) {
        Integer balance = fineRepository.sumBalanceByPatronId(patronId);
        if (balance != null && balance <= 2000) {
            Patron patron = patronService.findById(patronId);
            if (patron.getStatus() == com.escola.biblioteca.domain.model.PatronStatus.BLOCKED) {
                patron.setStatus(com.escola.biblioteca.domain.model.PatronStatus.ACTIVE);
                // PatronRepository.save(patron) would be called via PatronService
            }
        }
    }

    private void publishEvent(UUID aggregateId, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Fine");
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(com.escola.biblioteca.util.JsonUtil.toJson(payload));
        event.setCreatedAt(java.time.OffsetDateTime.now());
        event.marcarNovo();
        outboxEventRepository.save(event);
    }
}