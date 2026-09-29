package com.escola.biblioteca.dashboard;

import com.escola.biblioteca.dto.response.DashboardResponse;
import com.escola.biblioteca.service.DashboardService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardBroadcaster {

    private static final long NO_TIMEOUT = 0L;

    private final DashboardService dashboardService;
    private final ConcurrentHashMap.KeySetView<EmitterEntry, Boolean> emitters = ConcurrentHashMap.newKeySet();

    @Value("${app.sse.heartbeat-ms:15000}")
    private long heartbeatMs;

    public SseEmitter subscribe(UUID institutionId) {
        SseEmitter emitter = new SseEmitter(NO_TIMEOUT);
        var entry = new EmitterEntry(emitter, institutionId);
        emitters.add(entry);
        emitter.onCompletion(() -> remove(entry));
        emitter.onTimeout(() -> remove(entry));
        emitter.onError(e -> remove(entry));
        try {
            send(emitter, dashboardService.stats(institutionId));
        } catch (Exception e) {
            log.warn("Dashboard stats falhou no subscribe (institution={}): {}", institutionId, e.getMessage());
            send(emitter, DashboardResponse.empty());
        }
        return emitter;
    }

    public void broadcast() {
        emitters.forEach(entry -> {
            try {
                DashboardResponse stats = dashboardService.stats(entry.institutionId);
                send(entry.emitter, stats);
            } catch (Exception e) {
                log.debug("Dashboard stats falhou no broadcast (institution={}): {}", entry.institutionId, e.getMessage());
            }
        });
    }

    public int activeConnections() {
        return emitters.size();
    }

    private void send(SseEmitter emitter, DashboardResponse stats) {
        try {
            emitter.send(SseEmitter.event().name("counters").data(stats));
        } catch (Exception e) {
            log.debug("SSE send falhou, fechando tunnel: {}", e.getMessage());
        }
    }

    private void remove(EmitterEntry entry) {
        emitters.remove(entry);
        try {
            entry.emitter.complete();
        } catch (Exception ignored) {
        }
    }

    @Scheduled(fixedDelayString = "${app.sse.heartbeat-ms:15000}")
    public void heartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        emitters.forEach(e -> {
            try {
                e.emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception ex) {
                log.debug("SSE heartbeat falhou, removendo listener");
                remove(e);
            }
        });
    }

    private record EmitterEntry(SseEmitter emitter, UUID institutionId) {}
}
