package com.showbie.common.http.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class responsible for generating authentication JWS tokens.
 * Generates tokens readable by the {@link TokenParser}.
 */
public class TokenGenerator {
    /**
     * Generate a HS256 token.
     *
     * @param signingKey HS256 signing key.
     * @param issuedAt   Issued At time.
     * @param expiresAt  Expires At time.
     * @param scope      Optional scopes to include (you want to include at least one).
     * @return JWS signed token.
     */
    public static String createTokenHS256(String signingKey, Date issuedAt, Date expiresAt, String... scope) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scopes", Arrays.stream(scope).distinct().collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(SignatureAlgorithm.HS256, signingKey.getBytes())
                .compact();
    }

    /**
     * Generate a HS256 token.
     *
     * @param signingKey         HS256 signing key.
     * @param expiresAfterMillis Lifetime of the token in milliseconds.
     * @param scope              Optional scopes to include (you want to include at least one).
     * @return JWS signed token.
     */
    public static String createTokenHS256(String signingKey, long expiresAfterMillis, String... scope) {
        return createTokenHS256(
                signingKey,
                new Date(),
                new Date(System.currentTimeMillis() + expiresAfterMillis),
                scope
        );
    }
}
