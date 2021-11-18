package com.showbie.common.http.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convenience service to read scopes for the currently authenticated request.
 * Reads the token scopes added to the SecurityContext's authorities as stored
 * by the {@link TokenValidationFilter}.
 */
@Service
public class AuthenticatedTokenScopes {
    /**
     * Get the token scopes for the current authenticated request.
     * @return Set of scopes; is empty if not authenticated.
     */
    public Set<String> getScopes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            return authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
        }

        // not authenticated -- no scopes
        return Collections.emptySet();
    }
}
