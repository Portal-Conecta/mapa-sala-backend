package com.portal.conecta.mapa_de_sala.shared.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticatedUser authenticatedUser;

    public JwtAuthenticationToken(AuthenticatedUser authenticatedUser) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.profile().name())));
        this.authenticatedUser = authenticatedUser;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authenticatedUser;
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }
}
