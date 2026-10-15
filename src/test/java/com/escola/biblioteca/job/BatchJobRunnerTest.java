package com.escola.biblioteca.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchJobRunnerTest {

    @Mock
    private CirculationJobService jobService;

    @Mock
    private ApplicationContext applicationContext;

    private BatchJobRunner runner;

    @BeforeEach
    void setUp() {
        runner = new BatchJobRunner(jobService, applicationContext) {
            @Override
            protected void exitProcess(int code) {
                // Do not exit JVM in tests
            }
        };
    }

    @Test
    void whenNoJobArg_shouldDoNothing() {
        DefaultApplicationArguments args = new DefaultApplicationArguments();
        runner.run(args);
        verifyNoInteractions(jobService);
    }

    @Test
    void executeJob_dailyMaintenance_shouldInvokeService() {
        int code = runner.executeJob("daily-maintenance");
        assertEquals(0, code);
        verify(jobService).runDailyMaintenance();
    }

    @Test
    void executeJob_holdExpiry_shouldInvokeService() {
        int code = runner.executeJob("hold-expiry");
        assertEquals(0, code);
        verify(jobService).runHoldExpiry();
    }

    @Test
    void executeJob_unknown_shouldReturnErrorCode() {
        int code = runner.executeJob("invalid-job-name");
        assertEquals(1, code);
        verifyNoInteractions(jobService);
    }
}
