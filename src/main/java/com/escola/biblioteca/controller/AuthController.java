package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.LoginRequest;
import com.escola.biblioteca.dto.response.ApiError;
import com.escola.biblioteca.dto.response.LoginResponse;
import com.escola.biblioteca.dto.response.RefreshResponse;
import com.escola.biblioteca.dto.response.TokenPair;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.model.AdminRole;
import com.escola.biblioteca.domain.model.Institution;
import com.escola.biblioteca.repository.AdmRepository;
import com.escola.biblioteca.domain.repository.InstitutionRepository;
import com.escola.biblioteca.security.AdminAuthenticationToken;
import com.escola.biblioteca.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login, refresh, logout e informações do usuário autenticado")
public class AuthController {

    public static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;
    private final AdmRepository admRepository;
    private final InstitutionRepository institutionRepository;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Value("${app.jwt.cookie-secure}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site}")
    private String cookieSameSite;

    public AuthController(AuthService authService, AdmRepository admRepository,
                       InstitutionRepository institutionRepository) {
        this.authService = authService;
        this.admRepository = admRepository;
        this.institutionRepository = institutionRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um administrador e retorna o access token (JWT, 7 min). O refresh token é enviado via cookie httpOnly (7 dias).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit excedido (5 req/min por IP)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        TokenPair pair = authService.login(request, httpRequest);
        setRefreshCookie(httpResponse, pair.refreshToken());
        return ResponseEntity.ok(new LoginResponse(pair.accessToken(), pair.nome(), pair.login(),
                pair.role(), pair.institutionId()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Renova o access token usando o refresh token armazenado no cookie httpOnly. O refresh token é rotacionado a cada uso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token ausente, expirado ou revogado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit excedido (10 req/min por IP)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RefreshResponse> refresh(HttpServletRequest httpRequest,
                                                   HttpServletResponse httpResponse,
                                                   @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException("error.auth.refresh.missing");
        }
        TokenPair pair = authService.refresh(refreshToken, httpRequest);
        setRefreshCookie(httpResponse, pair.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(pair.accessToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga o refresh token atual e limpa o cookie. Não invalida o access token (expira em 7 min naturalmente).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "204", description = "Refresh token ausente (ignorado)")
    })
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse,
                                       @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearRefreshCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Revogar todas as sessões", description = "Revoga todos os refresh tokens do administrador autenticado. Requer role GLOBAL_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as sessões revogadas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (role insuficiente)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> logoutAll(HttpServletResponse httpResponse) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken upToken && upToken.getPrincipal() instanceof String login) {
            authService.logoutAll(login);
        }
        clearRefreshCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Informações do usuário autenticado", description = "Retorna login, nome, role e instituições do administrador autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuário"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<MeResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AdminAuthenticationToken adminAuth)) {
            return ResponseEntity.status(401).build();
        }

        var adm = admRepository.findByLogin(adminAuth.getLogin());
        if (adm.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        List<InstitutionSummary> institutions;
        if (adminAuth.isGlobalAdmin()) {
            var all = new ArrayList<Institution>();
            institutionRepository.findAll().forEach(all::add);
            institutions = all.stream()
                    .map(i -> new InstitutionSummary(i.getId(), i.getCode(), i.getName(), AdminRole.GLOBAL_ADMIN))
                    .toList();
        } else {
            UUID institutionId = adminAuth.getInstitutionId();
            if (institutionId != null) {
                var inst = institutionRepository.findById(institutionId);
                institutions = inst.map(i -> List.of(new InstitutionSummary(i.getId(), i.getCode(), i.getName(), AdminRole.INSTITUTION_ADMIN)))
                        .orElse(List.of());
            } else {
                institutions = institutionRepository.findInstitutionsByAdmLogin(adminAuth.getLogin())
                        .stream()
                        .map(i -> new InstitutionSummary(i.getId(), i.getCode(), i.getName(), AdminRole.INSTITUTION_ADMIN))
                        .toList();
            }
        }

        return ResponseEntity.ok(new MeResponse(adminAuth.getLogin(), adm.get().getNome(), adminAuth.getRole(), institutions));
    }

    public record MeResponse(String login, String nome, AdminRole role,
                             List<InstitutionSummary> institutions) {}

    public record InstitutionSummary(UUID id, String code, String name, AdminRole role) {}

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(Duration.ofDays(refreshExpirationDays))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
