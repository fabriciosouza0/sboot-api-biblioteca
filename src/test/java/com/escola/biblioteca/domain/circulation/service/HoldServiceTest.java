package com.escola.biblioteca.domain.circulation.service;

import com.escola.biblioteca.dashboard.DashboardPublisher;
import com.escola.biblioteca.domain.circulation.model.Hold;
import com.escola.biblioteca.domain.circulation.model.enums.HoldStatus;
import com.escola.biblioteca.domain.circulation.policy.LoanPolicy;
import com.escola.biblioteca.domain.patron.model.Patron;
import com.escola.biblioteca.domain.patron.model.enums.PatronProfile;
import com.escola.biblioteca.domain.patron.model.enums.PatronStatus;
import com.escola.biblioteca.domain.patron.service.PatronService;
import com.escola.biblioteca.domain.circulation.repository.HoldRepository;
import com.escola.biblioteca.domain.catalog.repository.ItemRepository;
import com.escola.biblioteca.domain.outbox.repository.OutboxEventRepository;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldServiceTest {

    @Mock
    private HoldRepository holdRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PatronService patronService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private DashboardPublisher dashboardPublisher;

    @InjectMocks
    private HoldService holdService;

    private UUID patronId;
    private UUID workId;
    private UUID libraryId;
    private Patron patron;
    private LoanPolicy policy;

    @BeforeEach
    void setUp() {
        patronId = UUID.randomUUID();
        workId = UUID.randomUUID();
        libraryId = UUID.randomUUID();

        patron = new Patron();
        patron.setId(patronId);
        patron.setStatus(PatronStatus.ACTIVE);
        patron.setFineBalance(0);
        patron.setProfile(PatronProfile.STUDENT);

        policy = new LoanPolicy(3, 7, 2, 5, 50, 5000);
    }

    @Test
    void placeHold_shouldSucceed_whenPatronActiveAndWithinLimit() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);
        when(holdRepository.findByPatronId(patronId)).thenReturn(List.of());
        when(holdRepository.findWaitingByWorkIdAndLibraryId(workId, libraryId)).thenReturn(List.of());
        when(holdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Hold hold = holdService.placeHold(patronId, workId, libraryId);

        assertNotNull(hold);
        assertEquals(patronId, hold.getPatronId());
        assertEquals(workId, hold.getWorkId());
        assertEquals(libraryId, hold.getLibraryId());
        assertEquals(HoldStatus.WAITING, hold.getStatus());
        assertEquals(1, hold.getPosition());
        assertNotNull(hold.getPlacedAt());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void placeHold_shouldFail_whenPatronBlocked() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> holdService.placeHold(patronId, workId, libraryId));
        assertEquals("error.patron.blocked", ex.getCode());
    }

    @Test
    void placeHold_shouldFail_whenHoldLimitExceeded() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);
        when(holdRepository.findByPatronId(patronId)).thenReturn(
                List.of(createHold(HoldStatus.WAITING), createHold(HoldStatus.WAITING),
                        createHold(HoldStatus.WAITING), createHold(HoldStatus.WAITING),
                        createHold(HoldStatus.READY)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> holdService.placeHold(patronId, workId, libraryId));
        assertEquals("error.patron.hold.limit.exceeded", ex.getCode());
    }

    @Test
    void placeHold_shouldCalculatePositionCorrectly() {
        when(patronService.findById(patronId)).thenReturn(patron);
        when(patronService.isBlocked(patron)).thenReturn(false);
        when(patronService.getLimits(patron)).thenReturn(policy);
        when(holdRepository.findByPatronId(patronId)).thenReturn(List.of());
        when(holdRepository.findWaitingByWorkIdAndLibraryId(workId, libraryId))
                .thenReturn(List.of(createHold(HoldStatus.WAITING), createHold(HoldStatus.WAITING)));
        when(holdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Hold hold = holdService.placeHold(patronId, workId, libraryId);

        assertEquals(3, hold.getPosition());
    }

    @Test
    void cancelHold_shouldSucceed_whenHoldWaiting() {
        Hold hold = createHold(HoldStatus.WAITING);
        hold.setId(UUID.randomUUID());

        when(holdRepository.findById(hold.getId())).thenReturn(Optional.of(hold));
        when(holdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(holdRepository.findWaitingByWorkIdAndLibraryId(workId, libraryId)).thenReturn(List.of());

        holdService.cancelHold(hold.getId());

        assertEquals(HoldStatus.CANCELLED, hold.getStatus());
        assertNotNull(hold.getCancelledAt());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void cancelHold_shouldFail_whenHoldFulfilled() {
        Hold hold = createHold(HoldStatus.FULFILLED);
        hold.setId(UUID.randomUUID());

        when(holdRepository.findById(hold.getId())).thenReturn(Optional.of(hold));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> holdService.cancelHold(hold.getId()));
        assertEquals("error.hold.cannot.cancel", ex.getCode());
    }

    @Test
    void fulfillHold_shouldSucceed_whenHoldReady() {
        Hold hold = createHold(HoldStatus.READY);
        hold.setId(UUID.randomUUID());
        hold.setWorkId(workId);
        hold.setLibraryId(libraryId);

        when(holdRepository.findById(hold.getId())).thenReturn(Optional.of(hold));
        when(holdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(holdRepository.findNextWaitingByWorkIdAndLibraryId(workId, libraryId)).thenReturn(Optional.empty());

        Hold fulfilled = holdService.fulfillHold(hold.getId());

        assertEquals(HoldStatus.FULFILLED, fulfilled.getStatus());
        assertNotNull(fulfilled.getFulfilledAt());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void fulfillHold_shouldFail_whenHoldNotReady() {
        Hold hold = createHold(HoldStatus.WAITING);
        hold.setId(UUID.randomUUID());

        when(holdRepository.findById(hold.getId())).thenReturn(Optional.of(hold));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> holdService.fulfillHold(hold.getId()));
        assertEquals("error.hold.not.ready", ex.getCode());
    }

//    @Test
//    void expireHolds_shouldExpireReadyHoldsAndNotifyNext() {
//        Hold expired = createHold(HoldStatus.READY);
//        expired.setId(UUID.randomUUID());
//        expired.setWorkId(workId);
//        expired.setLibraryId(libraryId);
//        expired.setExpiresAt(Instant.now().minusSeconds(3600));
//
//        Hold nextInQueue = createHold(HoldStatus.WAITING);
//        nextInQueue.setId(UUID.randomUUID());
//        nextInQueue.setWorkId(workId);
//        nextInQueue.setLibraryId(libraryId);
//
//        when(holdRepository.findReadyExpired()).thenReturn(List.of(expired));
//        when(holdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//        when(holdRepository.findNextWaitingByWorkIdAndLibraryId(workId, libraryId)).thenReturn(Optional.of(nextInQueue));
//
//        holdService.expireHolds();
//
//        assertEquals(HoldStatus.EXPIRED, expired.getStatus());
//        assertEquals(HoldStatus.READY, nextInQueue.getStatus());
//        assertNotNull(nextInQueue.getReadyAt());
//        assertNotNull(nextInQueue.getExpiresAt());
//        verify(outboxEventRepository, times(2)).save(any());
//    }

    private Hold createHold(HoldStatus status) {
        Hold hold = new Hold();
        hold.setStatus(status);
        hold.setPatronId(patronId);
        hold.setWorkId(workId);
        hold.setLibraryId(libraryId);
        return hold;
    }
}