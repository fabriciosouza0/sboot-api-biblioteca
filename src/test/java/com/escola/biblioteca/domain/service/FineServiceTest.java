package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Fine;
import com.escola.biblioteca.domain.model.FineStatus;
import com.escola.biblioteca.domain.model.FineType;
import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.model.PatronStatus;
import com.escola.biblioteca.domain.model.ProfileConfig;
import com.escola.biblioteca.domain.repository.FineRepository;
import com.escola.biblioteca.domain.repository.LoanRepository;
import com.escola.biblioteca.domain.repository.OutboxEventRepository;
import com.escola.biblioteca.domain.repository.ProfileConfigRepository;
import com.escola.biblioteca.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private PatronService patronService;

    @Mock
    private ProfileConfigRepository profileConfigRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private FineService fineService;

    private UUID patronId;
    private UUID loanId;
    private Patron patron;
    private Loan loan;
    private ProfileConfig config;

    @BeforeEach
    void setUp() {
        patronId = UUID.randomUUID();
        loanId = UUID.randomUUID();

        patron = new Patron();
        patron.setId(patronId);
        patron.setInstitutionId(UUID.randomUUID());
        patron.setProfile(PatronProfile.STUDENT);
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(0);

        loan = new Loan();
        loan.setId(loanId);
        loan.setPatronId(patronId);
        loan.setStatus(LoanStatus.OVERDUE);
        loan.setDueAt(Instant.now().minusSeconds(86400 * 5)); // 5 days ago

        config = new ProfileConfig();
        config.setFineRateCents(50);
        config.setFineCapCents(5000);
    }

    @Test
    void assessOverdue_shouldSucceed_whenLoanOverdue() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(profileConfigRepository.findByInstitutionIdAndProfile(patron.getInstitutionId(), patron.getProfile()))
                .thenReturn(Optional.of(config));
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Fine fine = fineService.assessOverdue(loanId);

        assertNotNull(fine);
        assertEquals(FineType.OVERDUE, fine.getType());
        assertEquals(FineStatus.PENDING, fine.getStatus());
        assertEquals(250, fine.getAmountCents()); // 5 days * 50 cents
        assertEquals(250, fine.getBalanceCents());
        assertNotNull(fine.getAssessedAt());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void assessOverdue_shouldCapAtMaxFine() {
        loan.setDueAt(Instant.now().minusSeconds(86400 * 200)); // 200 days ago -> 10000 cents, capped at 5000
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(profileConfigRepository.findByInstitutionIdAndProfile(patron.getInstitutionId(), patron.getProfile()))
                .thenReturn(Optional.of(config));
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Fine fine = fineService.assessOverdue(loanId);

        assertEquals(5000, fine.getAmountCents());
        assertEquals(5000, fine.getBalanceCents());
    }

    @Test
    void assessOverdue_shouldFail_whenLoanNotOverdue() {
        loan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fineService.assessOverdue(loanId));
        assertEquals("error.loan.not.overdue", ex.getCode());
    }

    @Test
    void assessOverdue_shouldFail_whenDaysOverdueZero() {
        loan.setDueAt(Instant.now().plusSeconds(3600)); // Due in future
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(profileConfigRepository.findByInstitutionIdAndProfile(patron.getInstitutionId(), patron.getProfile()))
                .thenReturn(Optional.of(config));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fineService.assessOverdue(loanId));
        assertEquals("error.loan.not.overdue", ex.getCode());
    }

    @Test
    void assessLost_shouldCreateFineWithReplacementCost() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Fine fine = fineService.assessLost(loanId, 3000);

        assertEquals(FineType.LOST, fine.getType());
        assertEquals(3000, fine.getAmountCents());
        assertEquals(3000, fine.getBalanceCents());
    }

    @Test
    void assessDamage_shouldCreateFineWithDamagePercent() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Fine fine = fineService.assessDamage(loanId, 1500);

        assertEquals(FineType.DAMAGE, fine.getType());
        assertEquals(1500, fine.getAmountCents());
    }

    @Test
    void pay_shouldSucceed_whenFullPayment() {
        Fine fine = new Fine();
        fine.setId(UUID.randomUUID());
        fine.setPatronId(patronId);
        fine.setType(FineType.OVERDUE);
        fine.setAmountCents(500);
        fine.setBalanceCents(500);
        fine.setStatus(FineStatus.PENDING);

        when(fineRepository.findById(fine.getId())).thenReturn(Optional.of(fine));
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepository.sumBalanceByPatronId(patronId)).thenReturn(0);
        when(patronService.findById(patronId)).thenReturn(patron);

        Fine paid = fineService.pay(fine.getId(), "pix-ref-123", 500);

        assertEquals(FineStatus.PAID, paid.getStatus());
        assertEquals(0, paid.getBalanceCents());
        assertNotNull(paid.getPaidAt());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void pay_shouldSucceed_whenPartialPayment() {
        Fine fine = new Fine();
        fine.setId(UUID.randomUUID());
        fine.setPatronId(patronId);
        fine.setType(FineType.OVERDUE);
        fine.setAmountCents(500);
        fine.setBalanceCents(500);
        fine.setStatus(FineStatus.PENDING);

        when(fineRepository.findById(fine.getId())).thenReturn(Optional.of(fine));
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepository.sumBalanceByPatronId(patronId)).thenReturn(300);
        when(patronService.findById(patronId)).thenReturn(patron);

        Fine paid = fineService.pay(fine.getId(), "pix-ref-123", 200);

        assertEquals(FineStatus.PARTIAL, paid.getStatus());
        assertEquals(300, paid.getBalanceCents());
    }

    @Test
    void pay_shouldFail_whenAlreadyPaid() {
        Fine fine = new Fine();
        fine.setId(UUID.randomUUID());
        fine.setStatus(FineStatus.PAID);

        when(fineRepository.findById(fine.getId())).thenReturn(Optional.of(fine));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fineService.pay(fine.getId(), "pix-ref", 100));
        assertEquals("error.fine.already.resolved", ex.getCode());
    }

    @Test
    void pay_shouldFail_whenAmountInvalid() {
        Fine fine = new Fine();
        fine.setId(UUID.randomUUID());
        fine.setStatus(FineStatus.PENDING);
        fine.setBalanceCents(500);

        when(fineRepository.findById(fine.getId())).thenReturn(Optional.of(fine));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> fineService.pay(fine.getId(), "pix-ref", 0));
        assertEquals("error.fine.invalid.amount", ex.getCode());

        ex = assertThrows(BusinessException.class,
                () -> fineService.pay(fine.getId(), "pix-ref", -100));
        assertEquals("error.fine.invalid.amount", ex.getCode());

        ex = assertThrows(BusinessException.class,
                () -> fineService.pay(fine.getId(), "pix-ref", 600));
        assertEquals("error.fine.overpayment", ex.getCode());
    }

    @Test
    void waive_shouldSetBalanceToZeroAndStatusWaived() {
        Fine fine = new Fine();
        fine.setId(UUID.randomUUID());
        fine.setPatronId(patronId);
        fine.setBalanceCents(500);
        fine.setStatus(FineStatus.PENDING);

        when(fineRepository.findById(fine.getId())).thenReturn(Optional.of(fine));
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepository.sumBalanceByPatronId(patronId)).thenReturn(0);
        when(patronService.findById(patronId)).thenReturn(patron);

        Fine waived = fineService.waive(fine.getId(), UUID.randomUUID(), "Isenção por carência");

        assertEquals(FineStatus.WAIVED, waived.getStatus());
        assertEquals(0, waived.getBalanceCents());
        assertNotNull(waived.getWaivedAt());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void writeOff_shouldSetBalanceToZeroAndStatusWrittenOff() {
        Fine fine = new Fine();
        fine.setId(UUID.randomUUID());
        fine.setPatronId(patronId);
        fine.setBalanceCents(500);
        fine.setStatus(FineStatus.PENDING);

        when(fineRepository.findById(fine.getId())).thenReturn(Optional.of(fine));
        when(fineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fineRepository.sumBalanceByPatronId(patronId)).thenReturn(0);
        when(patronService.findById(patronId)).thenReturn(patron);

        Fine writtenOff = fineService.writeOff(fine.getId(), UUID.randomUUID(), "Baixa contábil");

        assertEquals(FineStatus.WRITTEN_OFF, writtenOff.getStatus());
        assertEquals(0, writtenOff.getBalanceCents());
        assertNotNull(writtenOff.getWaivedAt());
    }
}