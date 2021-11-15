package com.showbie.publicservice.http.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a request authentication filter that validates the authorization
 * header's bearer token.
 */
@Component
public class TokenValidationFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Authenticate the request by validating the bearer token in the request's
     * authorization header.
     *
     * Guaranteed to be invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request Request object
     * @param response Response object
     * @param filterChain Authentication filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // clear any existing context
        SecurityContextHolder.clearContext();

        // ensure the request contains an authorization header including a bearer token
        // TODO: implement this
        TokenParser tokenParser = new TokenParser(null);

        // if token is valid then set security context
        if (tokenParser.isValid()) {
            logger.info("Authenticated user {} with scopes {}", tokenParser.getPrinciple(), tokenParser.getScopes());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(createAuthenticationPrinciple(tokenParser.getPrinciple(), tokenParser.getScopes()));
            SecurityContextHolder.setContext(context);
        }

        // continue the filter chain
        filterChain.doFilter(request, response);
    }

    Authentication createAuthenticationPrinciple(String email, List<String> scopes) {
        List<GrantedAuthority> authorities = scopes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        User user = new User(email, "", authorities);
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }
}

