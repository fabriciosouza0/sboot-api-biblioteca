package com.escola.biblioteca.security;

import com.escola.biblioteca.model.AdminRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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
                UUID institutionId = jwtService.extractInstitutionId(token);

                var authorities = List.<SimpleGrantedAuthority>of(
                        new SimpleGrantedAuthority("ROLE_" + role.name())
                );

                var auth = new AdminAuthenticationToken(login, role, institutionId, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
