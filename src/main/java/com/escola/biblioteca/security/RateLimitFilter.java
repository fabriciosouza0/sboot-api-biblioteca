package com.escola.biblioteca.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting em memória (Bucket4j) por IP para os endpoints de autenticação.
 * - POST /api/auth/refresh: 10 requisições/minuto
 * - POST /api/auth/login:   5 requisições/minuto
 * Responde 429 com Retry-After quando o limite é atingido.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String REFRESH_PATH = "/api/auth/refresh";
    private static final String LOGIN_PATH = "/api/auth/login";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final SecurityErrorWriter securityErrorWriter;

    private final boolean enabled;
    private final int refreshCapacity;
    private final int loginCapacity;

    public RateLimitFilter(SecurityErrorWriter securityErrorWriter,
                           @Value("${app.rate-limit.enabled:true}") boolean enabled,
                           @Value("${app.rate-limit.refresh-capacity:10}") int refreshCapacity,
                           @Value("${app.rate-limit.login-capacity:5}") int loginCapacity) {
        this.securityErrorWriter = securityErrorWriter;
        this.enabled = enabled;
        this.refreshCapacity = refreshCapacity;
        this.loginCapacity = loginCapacity;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!enabled || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        int capacity = switch (request.getRequestURI()) {
            case REFRESH_PATH -> refreshCapacity;
            case LOGIN_PATH -> loginCapacity;
            default -> 0;
        };

        if (capacity <= 0) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = remoteIp(request) + "|" + request.getRequestURI();
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(capacity));
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader("Retry-After", String.valueOf(60 / capacity));
        securityErrorWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "error.rate.limited");
    }

    private Bucket newBucket(int capacity) {
        // capacity tokens + refill completo a cada minuto
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String remoteIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
