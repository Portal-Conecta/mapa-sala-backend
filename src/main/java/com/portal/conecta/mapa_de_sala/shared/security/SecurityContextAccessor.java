package com.portal.conecta.mapa_de_sala.shared.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextAccessor {

    public AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken jwtToken)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return jwtToken.getAuthenticatedUser();
    }
}
