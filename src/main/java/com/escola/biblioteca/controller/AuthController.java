package com.escola.biblioteca.controller;

import com.escola.biblioteca.dto.request.LoginRequest;
import com.escola.biblioteca.dto.response.LoginResponse;
import com.escola.biblioteca.dto.response.RefreshResponse;
import com.escola.biblioteca.dto.response.TokenPair;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Value("${app.jwt.cookie-secure}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site}")
    private String cookieSameSite;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        TokenPair pair = authService.login(request, httpRequest);
        setRefreshCookie(httpResponse, pair.refreshToken());
        return ResponseEntity.ok(new LoginResponse(pair.accessToken(), pair.nome(), pair.login()));
    }

    @PostMapping("/refresh")
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
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse,
                                       @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearRefreshCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletResponse httpResponse) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken upToken && upToken.getPrincipal() instanceof String login) {
            authService.logoutAll(login);
        }
        clearRefreshCookie(httpResponse);
        return ResponseEntity.noContent().build();
    }

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