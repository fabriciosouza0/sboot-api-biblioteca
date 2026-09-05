package com.escola.biblioteca.dashboard;

import com.escola.biblioteca.dto.response.DashboardResponse;
import com.escola.biblioteca.service.DashboardService;
import java.util.Set;
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
    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    @Value("${app.sse.heartbeat-ms:15000}")
    private long heartbeatMs;

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(NO_TIMEOUT);
        emitters.add(emitter);
        emitter.onCompletion(() -> remove(emitter));
        emitter.onTimeout(() -> remove(emitter));
        emitter.onError(e -> remove(emitter));
        send(emitter, dashboardService.stats());
        return emitter;
    }

    public void broadcast(DashboardResponse stats) {
        emitters.forEach(e -> send(e, stats));
    }

    public int activeConnections() {
        return emitters.size();
    }

    private void send(SseEmitter emitter, DashboardResponse stats) {
        try {
            emitter.send(SseEmitter.event().name("counters").data(stats));
        } catch (Exception e) {
            log.debug("SSE send falhou, fechando tunnel: {}", e.getMessage());
            remove(emitter);
        }
    }

    private void remove(SseEmitter emitter) {
        emitters.remove(emitter);
        try {
            emitter.complete();
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
                e.send(SseEmitter.event().comment("ping"));
            } catch (Exception ex) {
                log.debug("SSE heartbeat falhou, removendo listener");
                remove(e);
            }
        });
    }
}