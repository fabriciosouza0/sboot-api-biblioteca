package com.escola.biblioteca.security;

import com.escola.biblioteca.model.AdminRole;
import com.escola.biblioteca.repository.AdmRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdmRepository admRepository;

    public JwtAuthenticationFilter(JwtService jwtService, AdmRepository admRepository) {
        this.jwtService = jwtService;
        this.admRepository = admRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValidAccessToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String login = jwtService.extractLogin(token);
                AdminRole role = jwtService.extractRole(token);
                UUID tokenInstitutionId = jwtService.extractInstitutionId(token);

                List<UUID> allowedInstitutionIds = resolveAllowedInstitutions(login, role, tokenInstitutionId);

                UUID currentInstitutionId = resolveCurrentInstitution(request, allowedInstitutionIds, tokenInstitutionId);

                var authorities = List.<SimpleGrantedAuthority>of(
                        new SimpleGrantedAuthority("ROLE_" + role.name())
                );

                var auth = new AdminAuthenticationToken(login, role, allowedInstitutionIds, currentInstitutionId, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<UUID> resolveAllowedInstitutions(String login, AdminRole role, UUID tokenInstitutionId) {
        if (role == AdminRole.GLOBAL_ADMIN) {
            return List.of();
        }
        var adm = admRepository.findByLogin(login);
        if (adm.isEmpty()) {
            return tokenInstitutionId != null ? List.of(tokenInstitutionId) : List.of();
        }
        List<UUID> fromJoinTable = admRepository.findInstitutionIdsByAdmCodigo(adm.get().getCodigo());
        if (!fromJoinTable.isEmpty()) {
            return fromJoinTable;
        }
        if (tokenInstitutionId != null) {
            return List.of(tokenInstitutionId);
        }
        return List.of();
    }

    private UUID resolveCurrentInstitution(HttpServletRequest request, List<UUID> allowed, UUID fallback) {
        String headerValue = request.getHeader("X-Institution-Id");
        if (headerValue != null && !headerValue.isBlank()) {
            try {
                UUID requested = UUID.fromString(headerValue);
                if (allowed.isEmpty() || allowed.contains(requested)) {
                    return requested;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (!allowed.isEmpty()) {
            return allowed.get(0);
        }
        return fallback;
    }
}
