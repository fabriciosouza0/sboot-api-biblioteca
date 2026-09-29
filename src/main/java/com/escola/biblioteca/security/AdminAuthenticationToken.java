package com.escola.biblioteca.security;

import com.escola.biblioteca.model.AdminRole;
import java.util.Collection;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class AdminAuthenticationToken extends AbstractAuthenticationToken {

    private final String login;
    private final AdminRole role;
    private final UUID institutionId;

    public AdminAuthenticationToken(String login, AdminRole role, UUID institutionId,
                                    Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.login = login;
        this.role = role;
        this.institutionId = institutionId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return login;
    }

    public String getLogin() {
        return login;
    }

    public AdminRole getRole() {
        return role;
    }

    public UUID getInstitutionId() {
        return institutionId;
    }

    public boolean isGlobalAdmin() {
        return role == AdminRole.GLOBAL_ADMIN;
    }
}