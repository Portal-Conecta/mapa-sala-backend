package com.portal.conecta.mapa_de_sala.shared.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.portal.conecta.mapa_de_sala.shared.context.ContextClass;
import com.portal.conecta.mapa_de_sala.shared.context.RequestContext;
import com.portal.conecta.mapa_de_sala.shared.context.TypeUser;

public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final TypeUser userType;
    private final List<ContextClass> classes;
    private final List<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String userId, TypeUser userType, List<ContextClass> classes) {
        this.userId = userId;
        this.userType = userType;
        this.classes = classes;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_"+userType));
    }

    public RequestContext toRequestContext() {
        return new RequestContext(
                UUID.fromString(userId),
                userType,
                classes == null ? List.of() : classes
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
