package com.courthub.analytics.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UUID userId;

    public JwtAuthenticationToken(UUID userId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
