package com.escola.biblioteca.security;

import com.escola.biblioteca.model.Adm;
import com.escola.biblioteca.model.enums.AdminRole;
import com.escola.biblioteca.repository.AdmRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AdmRepository admRepository;

    @Mock
    private SecurityErrorWriter securityErrorWriter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, admRepository, securityErrorWriter);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void globalAdmin_withHeaderInstitution_setsCurrentInstitution() throws Exception {
        UUID instId = UUID.randomUUID();
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getHeader("X-Institution-Id")).thenReturn(instId.toString());
        when(jwtService.isValidAccessToken("valid-token")).thenReturn(true);
        when(jwtService.extractLogin("valid-token")).thenReturn("admin");
        when(jwtService.extractRole("valid-token")).thenReturn(AdminRole.GLOBAL_ADMIN);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertInstanceOf(AdminAuthenticationToken.class, auth);
        AdminAuthenticationToken adminAuth = (AdminAuthenticationToken) auth;
        assertTrue(adminAuth.isGlobalAdmin());
        assertEquals(instId, adminAuth.getCurrentInstitutionId());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void institutionAdmin_withAllowedHeaderInstitution_success() throws Exception {
        UUID allowedInstId = UUID.randomUUID();
        Adm adm = new Adm();
        adm.setCodigo(1);
        adm.setLogin("escola_admin");

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getHeader("X-Institution-Id")).thenReturn(allowedInstId.toString());
        when(jwtService.isValidAccessToken("valid-token")).thenReturn(true);
        when(jwtService.extractLogin("valid-token")).thenReturn("escola_admin");
        when(jwtService.extractRole("valid-token")).thenReturn(AdminRole.INSTITUTION_ADMIN);
        when(admRepository.findByLogin("escola_admin")).thenReturn(Optional.of(adm));
        when(admRepository.findInstitutionIdsByAdmCodigo(1)).thenReturn(List.of(allowedInstId));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        AdminAuthenticationToken adminAuth = (AdminAuthenticationToken) auth;
        assertFalse(adminAuth.isGlobalAdmin());
        assertEquals(allowedInstId, adminAuth.getCurrentInstitutionId());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void institutionAdmin_withoutHeader_fallsBackToAllowedInstitution() throws Exception {
        UUID allowedInstId = UUID.randomUUID();
        Adm adm = new Adm();
        adm.setCodigo(1);
        adm.setLogin("escola_admin");

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getHeader("X-Institution-Id")).thenReturn(null);
        when(jwtService.isValidAccessToken("valid-token")).thenReturn(true);
        when(jwtService.extractLogin("valid-token")).thenReturn("escola_admin");
        when(jwtService.extractRole("valid-token")).thenReturn(AdminRole.INSTITUTION_ADMIN);
        when(admRepository.findByLogin("escola_admin")).thenReturn(Optional.of(adm));
        when(admRepository.findInstitutionIdsByAdmCodigo(1)).thenReturn(List.of(allowedInstId));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        AdminAuthenticationToken adminAuth = (AdminAuthenticationToken) auth;
        assertFalse(adminAuth.isGlobalAdmin());
        assertEquals(allowedInstId, adminAuth.getCurrentInstitutionId());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void institutionAdmin_withUnallowedHeader_returnsForbidden() throws Exception {
        UUID allowedInstId = UUID.randomUUID();
        UUID otherInstId = UUID.randomUUID();
        Adm adm = new Adm();
        adm.setCodigo(1);
        adm.setLogin("escola_admin");

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(request.getHeader("X-Institution-Id")).thenReturn(otherInstId.toString());
        when(jwtService.isValidAccessToken("valid-token")).thenReturn(true);
        when(jwtService.extractLogin("valid-token")).thenReturn("escola_admin");
        when(jwtService.extractRole("valid-token")).thenReturn(AdminRole.INSTITUTION_ADMIN);
        when(admRepository.findByLogin("escola_admin")).thenReturn(Optional.of(adm));
        when(admRepository.findInstitutionIdsByAdmCodigo(1)).thenReturn(List.of(allowedInstId));

        filter.doFilterInternal(request, response, filterChain);

        verify(securityErrorWriter).write(eq(request), eq(response), eq(HttpStatus.FORBIDDEN), eq("error.accessdenied"));
        verify(filterChain, never()).doFilter(any(), any());
    }
}
