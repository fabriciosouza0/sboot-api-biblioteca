package com.escola.biblioteca.domain.service;

import com.escola.biblioteca.domain.model.Item;
import com.escola.biblioteca.domain.model.ItemStatus;
import com.escola.biblioteca.domain.model.Loan;
import com.escola.biblioteca.domain.model.LoanStatus;
import com.escola.biblioteca.domain.model.Patron;
import com.escola.biblioteca.domain.model.PatronProfile;
import com.escola.biblioteca.domain.model.PatronStatus;
import com.escola.biblioteca.domain.repository.ItemRepository;
import com.escola.biblioteca.domain.repository.LoanRepository;
import com.escola.biblioteca.domain.repository.OutboxEventRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PatronService patronService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private LoanService loanService;

    private UUID patronId;
    private UUID itemId;
    private UUID libraryId;
    private Patron patron;
    private Item item;
    private LoanPolicy policy;

    @BeforeEach
    void setUp() {
        patronId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        libraryId = UUID.randomUUID();

        patron = new Patron();
        patron.setId(patronId);
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(0);
        patron.setProfile(PatronProfile.STUDENT);

        item = new Item();
        item.setId(itemId);
        item.setStatus(ItemStatus.AVAILABLE);

        policy = new LoanPolicy(3, 7, 2, 5, 50, 5000);
    }

    @Test
    void checkout_shouldSucceed_whenPatronActiveAndItemAvailable() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);
        when(loanRepository.findByPatronIdAndStatusIn(any(), any())).thenReturn(List.of());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.checkout(patronId, itemId, libraryId);

        assertNotNull(loan);
        assertEquals(patronId, loan.getPatronId());
        assertEquals(itemId, loan.getItemId());
        assertEquals(libraryId, loan.getLibraryId());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertNotNull(loan.getCheckedOutAt());
        assertNotNull(loan.getDueAt());
        assertEquals(0, loan.getRenewalCount());

        verify(itemRepository).save(argThat(i -> i.getStatus() == ItemStatus.ON_LOAN));
        verify(outboxEventRepository).save(any());
    }

    @Test
    void checkout_shouldFail_whenPatronBlocked() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.checkout(patronId, itemId, libraryId));
        assertEquals("error.patron.blocked", ex.getCode());
    }

    @Test
    void checkout_shouldFail_whenItemNotAvailable() {
        item.setStatus(ItemStatus.ON_LOAN);
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.checkout(patronId, itemId, libraryId));
        assertEquals("error.item.not.available", ex.getCode());
    }

    @Test
    void checkout_shouldFail_whenLoanLimitExceeded() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);
        when(loanRepository.findByPatronIdAndStatusIn(any(), any())).thenReturn(List.of(new Loan(), new Loan(), new Loan()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.checkout(patronId, itemId, libraryId));
        assertEquals("error.patron.loan.limit.exceeded", ex.getCode());
    }

    @Test
    void returnLoan_shouldSucceed_whenLoanActive() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setPatronId(patronId);
        loan.setItemId(itemId);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueAt(OffsetDateTime.now().plusSeconds(3600));

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Loan returned = loanService.returnLoan(loan.getId(), libraryId, "OK");

        assertEquals(LoanStatus.RETURNED, returned.getStatus());
        assertNotNull(returned.getReturnedAt());
        assertEquals(libraryId, returned.getReturnedLibraryId());
        assertEquals(ItemStatus.AVAILABLE, item.getStatus());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void returnLoan_shouldSetItemToInRepair_whenConditionDamaged() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setPatronId(patronId);
        loan.setItemId(itemId);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueAt(OffsetDateTime.now().plusSeconds(3600));

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Loan returned = loanService.returnLoan(loan.getId(), libraryId, "DAMAGED");

        assertEquals(ItemStatus.IN_REPAIR, item.getStatus());
    }

    @Test
    void returnLoan_shouldFail_whenAlreadyReturned() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setStatus(LoanStatus.RETURNED);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.returnLoan(loan.getId(), libraryId, "OK"));
        assertEquals("error.loan.already.returned", ex.getCode());
    }

    @Test
    void renew_shouldSucceed_whenWithinLimits() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setPatronId(patronId);
        loan.setItemId(itemId);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueAt(OffsetDateTime.now().plusSeconds(3600));
        loan.setRenewalCount(0);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Loan renewed = loanService.renew(loan.getId());

        assertEquals(1, renewed.getRenewalCount());
        assertNotNull(renewed.getDueAt());
        assertEquals(LoanStatus.ACTIVE, renewed.getStatus());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void renew_shouldFail_whenMaxRenewalsExceeded() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setPatronId(patronId);
        loan.setItemId(itemId);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueAt(OffsetDateTime.now().plusSeconds(3600));
        loan.setRenewalCount(2);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.renew(loan.getId()));
        assertEquals("error.loan.renewal.limit.exceeded", ex.getCode());
    }

    @Test
    void renew_shouldFail_whenLoanNotActive() {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID());
        loan.setPatronId(patronId);
        loan.setItemId(itemId);
        loan.setStatus(LoanStatus.RETURNED);

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> loanService.renew(loan.getId()));
        assertEquals("error.loan.not.active", ex.getCode());
    }
}