package com.showbie.common.http.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements a request authentication filter that validates the authorization
 * header's bearer token.
 * <p>
 * Also extracts the correlation id from the incoming request and stores it for
 * logging and subsequent outgoing requests. This allows us to observe via
 * logging incoming requests as they travel through our services.
 * See also {@link com.showbie.common.http.correlation.RestTemplateCorrelationInterceptor}.
 */
@Component
public class TokenValidationFilter extends OncePerRequestFilter {
    public static String CORRELATION_ID_HEADER = "X-CorrelationId";
    public static String CORRELATION_MDC_KEY = "correlationId";
    private static final String AUTHORIZATION_HEADER_BEARER_PREFIX = "Bearer ";
    private static String AUTHORIZATION_HEADER_NAME = "Authorization";

    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${auth.token.key}") // startup will be halted if this is undefined
    private String tokenVerificationKey;

    @Value("#{'${auth.token.scopes}'.split('\\s*,\\s*')}") // startup will be halted if this is undefined
    private List<String> supportedScopes;

    @PostConstruct
    public void showInfo() {
        // log the (obfuscated) token key and supportedScopes for diagnosing configuration issues
        int minLength = 6;
        String obfuscatedKey = tokenVerificationKey;
        if (tokenVerificationKey.length() > minLength) { // all secure keys should be of longer length
            obfuscatedKey = tokenVerificationKey.substring(0, minLength) + "...";
        }
        logger.info("DIAGNOSTIC: Accepting key={} with supportedScopes={}", obfuscatedKey, supportedScopes);
    }

    /**
     * Authenticate the request by validating the bearer token in the request's
     * authorization header.
     * <p>
     * Guaranteed to be invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request     HTTP request object
     * @param response    HTTP response object
     * @param filterChain Authentication filter chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // clear any existing context
        SecurityContextHolder.clearContext();

        // store the request's correlation id for tracking purposes -- this
        // needs to be done here (instead of another filter) to ensure we log
        // the id for authentication related logging
        storeRequestCorrelationId(request);

        // ensure the request contains an authorization header including a bearer token
        String header = request.getHeader(AUTHORIZATION_HEADER_NAME);
        if (header != null && header.startsWith(AUTHORIZATION_HEADER_BEARER_PREFIX)) {
            com.showbie.common.http.security.TokenParser tokenParser = new com.showbie.common.http.security.TokenParser(
                    tokenVerificationKey,
                    supportedScopes,
                    header.substring(AUTHORIZATION_HEADER_BEARER_PREFIX.length())
            );

            // if token is valid then set security context
            if (tokenParser.isValid()) {
                // TODO - this could be abstracted to a method with a unit test to verify it and createAuthenticationPrinciple()
                logger.debug("Request authenticated via JWT with scopes {}", tokenParser.getScopes());
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(createAuthenticationPrinciple(tokenParser.getScopes()));
                SecurityContextHolder.setContext(context);
            }
        }

        // continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Store the request's correlation id so that it can be logged here and on
     * nested http requests to other services.
     *
     * @param request HTTP request object.
     */
    private void storeRequestCorrelationId(HttpServletRequest request) {
        // DOC - the correlation id is stored in the logging subsystem's MDC
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasLength(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            logger.warn("Request correlation id not supplied, using {}", correlationId);
        }
        MDC.put(CORRELATION_MDC_KEY, correlationId);
    }

    /**
     * Generate an authentication principle for use in SpringFramework.
     *
     * @param scopes Valid scopes.
     * @return Authentication principle.
     */
    private Authentication createAuthenticationPrinciple(List<String> scopes) {
        List<GrantedAuthority> authorities = scopes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        // token does not currently contain a principle name so we have no username or password
        User user = new User("authenticated", "", authorities);
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }
}

