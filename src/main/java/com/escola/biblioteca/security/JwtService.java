package com.escola.biblioteca.security;

import com.escola.biblioteca.model.enums.AdminRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32; // 256 bits

    private final List<SecretKey> keys;
    private final long expirationMs;

    /**
     * Aceita a chave atual ({@code app.jwt.secret}) e, opcionalmente, a chave
     * anterior ({@code app.jwt.previous-secret}) para rotação sem downtime:
     * tokens antigos continuam válidos enquanto a chave anterior estiver configurada,
     * mas novos tokens são sempre assinados com a chave atual (keys.get(0)).
     */
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.previous-secret:}") String previousSecret,
                      @Value("${app.jwt.access-expiration-ms}") long expirationMs) {
        List<SecretKey> all = new java.util.ArrayList<>();
        all.add(resolveKey(secret));
        SecretKey previous = resolveOptional(previousSecret);
        if (previous != null && !previous.equals(all.get(0))) {
            all.add(previous);
        }
        this.keys = all;
        this.expirationMs = expirationMs;
    }

    public String generateAccessToken(String login, AdminRole role, UUID institutionId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(login)
                .claim("type", "access")
                .claim("role", role.name())
                .claim("institutionId", institutionId != null ? institutionId.toString() : null)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(keys.get(0))
                .compact();
    }

    public boolean isValidAccessToken(String token) {
        try {
            return "access".equals(parseWithAnyKey(token).get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String extractLogin(String token) {
        return parseWithAnyKey(token).getSubject();
    }

    public AdminRole extractRole(String token) {
        String roleStr = parseWithAnyKey(token).get("role", String.class);
        return roleStr != null ? AdminRole.valueOf(roleStr) : AdminRole.INSTITUTION_ADMIN;
    }

    public UUID extractInstitutionId(String token) {
        String instStr = parseWithAnyKey(token).get("institutionId", String.class);
        return instStr != null ? UUID.fromString(instStr) : null;
    }

    private SecretKey resolveOptional(String secret) {
        if (secret == null || secret.isBlank()) {
            return null;
        }
        return resolveKey(secret);
    }

    /**
     * Decodifica o secret (base64, conforme openssl rand -base64 32; fallback para
     * bytes UTF-8 crus) e garante no mínimo 256 bits — falha rápida no startup.
     */
    private SecretKey resolveKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET não definido. Gere um com: openssl rand -base64 32");
        }
        byte[] bytes = decodeBase64(secret.trim());
        if (bytes.length < MIN_SECRET_BYTES) {
            bytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET deve ter no mínimo 256 bits (32 bytes). Gere com: openssl rand -base64 32");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    private byte[] decodeBase64(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            return new byte[0];
        }
    }

    private Claims parseWithAnyKey(String token) {
        io.jsonwebtoken.JwtException lastError = null;
        for (SecretKey key : keys) {
            try {
                return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            } catch (io.jsonwebtoken.JwtException e) {
                lastError = e;
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new io.jsonwebtoken.JwtException("Nenhuma chave disponível para validar o token");
    }
}
