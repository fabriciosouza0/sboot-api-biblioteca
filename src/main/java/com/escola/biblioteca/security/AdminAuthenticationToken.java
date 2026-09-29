package com.escola.biblioteca.security;

import com.escola.biblioteca.model.AdminRole;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class AdminAuthenticationToken extends AbstractAuthenticationToken {

    private final String login;
    private final AdminRole role;
    private final List<UUID> allowedInstitutionIds;
    private UUID currentInstitutionId;

    public AdminAuthenticationToken(String login, AdminRole role, List<UUID> allowedInstitutionIds,
                                    UUID currentInstitutionId,
                                    Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.login = login;
        this.role = role;
        this.allowedInstitutionIds = allowedInstitutionIds;
        this.currentInstitutionId = currentInstitutionId;
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

    public List<UUID> getAllowedInstitutionIds() {
        return allowedInstitutionIds;
    }

    public UUID getCurrentInstitutionId() {
        return currentInstitutionId;
    }

    public void setCurrentInstitutionId(UUID currentInstitutionId) {
        this.currentInstitutionId = currentInstitutionId;
    }

    public boolean isGlobalAdmin() {
        return role == AdminRole.GLOBAL_ADMIN;
    }
}
