package com.portal.conecta.mapa_de_sala.shared.context;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.portal.conecta.mapa_de_sala.shared.exception.UnauthorizedUserException;

@Component
public class SecurityRequestContextProvider implements RequestContextProvider {

    @Override
    public RequestContext getRequestContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedUserException("Authentication is required.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof RequestContext requestContext) {
            return requestContext;
        }

        throw new UnauthorizedUserException("Authenticated request context is not available.");
    }
}