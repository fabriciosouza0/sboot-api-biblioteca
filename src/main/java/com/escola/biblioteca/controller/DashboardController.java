package com.escola.biblioteca.controller;

import com.escola.biblioteca.dashboard.DashboardBroadcaster;
import com.escola.biblioteca.security.AdminAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardBroadcaster broadcaster;

    public DashboardController(DashboardBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpServletRequest request) {
        UUID institutionId = resolveInstitutionId(request);
        return broadcaster.subscribe(institutionId);
    }

    private UUID resolveInstitutionId(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AdminAuthenticationToken adminAuth) {
            if (!adminAuth.isGlobalAdmin()) {
                return adminAuth.getCurrentInstitutionId();
            }
            String header = request.getHeader("X-Institution-Id");
            if (header != null && !header.isBlank()) {
                try {
                    return UUID.fromString(header);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }
}
