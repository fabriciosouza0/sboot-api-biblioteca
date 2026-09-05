package com.escola.biblioteca.dashboard;

import com.escola.biblioteca.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DashboardEventListener {

    private final DashboardService dashboardService;
    private final DashboardBroadcaster broadcaster;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDashboardChanged(DashboardChangedEvent event) {
        broadcaster.broadcast(dashboardService.stats());
    }
}