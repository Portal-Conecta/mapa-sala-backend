package com.portal.conecta.mapa_de_sala.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_PROFILE_HEADER = "X-User-Profile";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String profileHeader = request.getHeader(USER_PROFILE_HEADER);

        if (userIdHeader != null && profileHeader != null) {
            UUID userId = UUID.fromString(userIdHeader);
            UserProfile profile = UserProfile.valueOf(profileHeader);
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(new AuthenticatedUser(userId, profile)));
        }

        filterChain.doFilter(request, response);
    }
}
