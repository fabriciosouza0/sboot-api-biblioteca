package com.escola.biblioteca.job;

import com.escola.biblioteca.domain.circulation.service.FineService;
import com.escola.biblioteca.domain.circulation.service.HoldService;
import com.escola.biblioteca.domain.circulation.service.LoanService;
import com.escola.biblioteca.domain.patron.service.PatronService;
import com.escola.biblioteca.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CirculationJobServiceTest {

    @Mock
    private HoldService holdService;

    @Mock
    private LoanService loanService;

    @Mock
    private FineService fineService;

    @Mock
    private PatronService patronService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private CirculationJobService jobService;

    @Test
    void runDailyMaintenance_shouldExecuteAllSubtasksSequentially() {
        when(loanService.detectAndMarkOverdues(any(OffsetDateTime.class))).thenReturn(5);
        when(fineService.accrueOverdueFines(any(OffsetDateTime.class))).thenReturn(4);
        when(patronService.updatePatronBlockStatuses()).thenReturn(2);
        when(holdService.expireHolds(any(OffsetDateTime.class))).thenReturn(3);
        when(loanService.autoRenewEligibleLoans(any(OffsetDateTime.class), eq(3))).thenReturn(1);

        CirculationJobService.MaintenanceReport report = jobService.runDailyMaintenance();

        assertNotNull(report);
        assertEquals(5, report.overduesMarked());
        assertEquals(4, report.finesProcessed());
        assertEquals(2, report.patronsBlocked());
        assertEquals(3, report.holdsExpired());
        assertEquals(1, report.loansRenewed());
        assertTrue(report.durationMs() >= 0);

        verify(loanService).detectAndMarkOverdues(any(OffsetDateTime.class));
        verify(fineService).accrueOverdueFines(any(OffsetDateTime.class));
        verify(patronService).updatePatronBlockStatuses();
        verify(holdService).expireHolds(any(OffsetDateTime.class));
        verify(loanService).autoRenewEligibleLoans(any(OffsetDateTime.class), eq(3));
        verify(refreshTokenService).cleanExpiredOrRevoked();
    }

    @Test
    void runIndividualJobs_shouldCallRespectiveServices() {
        when(holdService.expireHolds(any())).thenReturn(2);
        assertEquals(2, jobService.runHoldExpiry());

        when(loanService.detectAndMarkOverdues(any())).thenReturn(3);
        assertEquals(3, jobService.runOverdueDetection());

        when(fineService.accrueOverdueFines(any())).thenReturn(1);
        assertEquals(1, jobService.runFineAccrual());

        when(loanService.autoRenewEligibleLoans(any(), eq(3))).thenReturn(4);
        assertEquals(4, jobService.runAutoRenewal());

        when(patronService.updatePatronBlockStatuses()).thenReturn(5);
        assertEquals(5, jobService.runPatronBlockCheck());

        jobService.runCleanup();
        verify(refreshTokenService).cleanExpiredOrRevoked();
    }
}
