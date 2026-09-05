package com.escola.biblioteca.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void dadosAlterados() {
        eventPublisher.publishEvent(new DashboardChangedEvent());
    }
}