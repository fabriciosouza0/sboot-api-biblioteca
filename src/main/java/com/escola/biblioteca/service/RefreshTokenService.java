package com.escola.biblioteca.service;

import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.model.Adm;
import com.escola.biblioteca.model.RefreshToken;
import com.escola.biblioteca.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Transactional
    public String issue(Adm adm, HttpServletRequest request) {
        String token = generateOpaqueToken();
        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(sha256(token));
        entity.setAdmId(adm.getCodigo());
        entity.setUserAgent(truncate(request != null ? request.getHeader("User-Agent") : null, 500));
        entity.setIp(remoteIp(request));
        entity.setExpiresAt(LocalDateTime.now().plusDays(refreshExpirationDays));
        entity.setCreatedAt(LocalDateTime.now());
        repository.save(entity);
        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateAndGet(String token) {
        RefreshToken stored = repository.findByTokenHashAndRevokedAtIsNull(sha256(token))
                .orElseThrow(() -> new BusinessException("error.auth.refresh.invalid"));
        if (stored.isExpired()) {
            throw new BusinessException("error.auth.refresh.invalid");
        }
        return stored;
    }

    @Transactional
    public void revoke(String token) {
        repository.findByTokenHashAndRevokedAtIsNull(sha256(token))
                .ifPresent(t -> {
                    t.setRevokedAt(LocalDateTime.now());
                    repository.save(t);
                });
    }

    @Transactional
    public void revokeAllByAdmId(Integer admId) {
        repository.revokeAllActiveByAdmId(admId, LocalDateTime.now());
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanExpiredOrRevoked() {
        repository.deleteExpiredOrRevoked(LocalDateTime.now());
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private String remoteIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
