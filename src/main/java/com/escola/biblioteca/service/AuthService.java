package com.escola.biblioteca.service;

import com.escola.biblioteca.dto.request.LoginRequest;
import com.escola.biblioteca.dto.response.TokenPair;
import com.escola.biblioteca.exception.BusinessException;
import com.escola.biblioteca.model.Adm;
import com.escola.biblioteca.model.RefreshToken;
import com.escola.biblioteca.repository.AdmRepository;
import com.escola.biblioteca.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdmRepository admRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenPair login(LoginRequest request, HttpServletRequest httpRequest) {
        Adm adm = authenticate(request);
        return issueTokenPair(adm, httpRequest);
    }

    @Transactional
    public TokenPair refresh(String refreshToken, HttpServletRequest httpRequest) {
        RefreshToken stored = refreshTokenService.validateAndGet(refreshToken);
        // Rotação estrita: o refresh token usado é revogado e nunca pode ser reutilizado.
        refreshTokenService.revoke(refreshToken);
        Adm adm = admRepository.findById(stored.getAdmId())
                .orElseThrow(() -> new BusinessException("error.auth.refresh.invalid"));
        return issueTokenPair(adm, httpRequest);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Transactional
    public void logoutAll(String login) {
        admRepository.findByLogin(login)
                .ifPresent(adm -> refreshTokenService.revokeAllByAdmId(adm.getCodigo()));
    }

    private TokenPair issueTokenPair(Adm adm, HttpServletRequest httpRequest) {
        String access = jwtService.generateAccessToken(adm.getLogin());
        String refresh = refreshTokenService.issue(adm, httpRequest);
        return new TokenPair(access, refresh, adm.getNome(), adm.getLogin());
    }

    private Adm authenticate(LoginRequest request) {
        Adm adm = admRepository.findByLogin(request.login())
                .orElseThrow(() -> new BusinessException("error.auth.credenciais"));

        boolean senhaOk = passwordEncoder.matches(request.senha(), adm.getSenha());
        if (!senhaOk && !isBcrypt(adm.getSenha())) {
            // Migração de senhas em texto puro (banco legado) para BCrypt.
            senhaOk = adm.getSenha().equals(request.senha());
            if (senhaOk) {
                adm.setSenha(passwordEncoder.encode(request.senha()));
                admRepository.save(adm);
            }
        }
        if (!senhaOk) {
            throw new BusinessException("error.auth.credenciais");
        }
        return adm;
    }

    private boolean isBcrypt(String senha) {
        return senha != null && (senha.startsWith("$2a$") || senha.startsWith("$2b$"));
    }
}
