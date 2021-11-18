package com.showbie.common.http.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.*;

/**
 * Utility class to parse and validate a (signed) JWT token.
 */
public class TokenParser {
    /* DOC - Normally this would be implemented as a service, but here we want
     *       it to store state (although this could be refactored to return
     *       the scopes only if the token is valid). In some circumstances a
     *       service providing an instance of this class (factory pattern)
     *       would be better. It depends on how the code is to be used -- for
     *       this assignment all would work equally well.
     */

    Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean isValid;
    private final List<String> scopes;

    /**
     * Parses a digitally signed JWT token.
     *
     * @param verificationKey Verification signing key.
     * @param token           Signed JWT string.
     */
    public TokenParser(@NonNull String verificationKey, @NonNull Collection<String> supportedScopes, @NonNull String token) {
        // GOAL - verify the token signature
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parser()
                    .setSigningKey(verificationKey.getBytes())
                    .parseClaimsJws(token);
        } catch (JwtException e) {
            // parsing failed for any of many reasons
            logger.warn("Token parsing failure: {}", e.getMessage());
            isValid = false;
            scopes = null;
            return;
        }

        // CLAIM - token was cryptographically signed by the supplied key its claims are good

        // DOC - however, we must still validate our semantic knowledge of the claims

        // GOAL - verify payload timestamps -- expiry time was verified above (if supplied)
        Date issuedAt = claimsJws.getBody().getIssuedAt();
        Date expiresBy = claimsJws.getBody().getExpiration();
        long now = new Date().getTime();

        // timestamps are valid if they were provided and issuedAt <= now < expiresBy
        boolean validTimestamps = issuedAt != null && expiresBy != null
                && issuedAt.getTime() <= now && now < expiresBy.getTime();
        if (!validTimestamps) {
            logger.warn("Token parsing failure: required timestamps missing or out of range: iat={}, exp={}", issuedAt, expiresBy);
        }

        // GOAL - verify payload scopes
        scopes = claimsJws.getBody().get("scopes", ArrayList.class);

        // scopes are valid if not empty and all are supported
        boolean validScopes = scopes != null && !scopes.isEmpty() && supportedScopes.containsAll(scopes);
        if (!validScopes) {
            logger.warn("Token parsing failure: missing or unsupported scopes={}", scopes);
        }

        isValid = validTimestamps && validScopes;
    }

    /**
     * Is the token valid?
     *
     * @return {@code true} if signed and contains valid claims, else {@code false}.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Get the valid token scopes.
     *
     * @return List of validated scopes.
     */
    public List<String> getScopes() {
        return isValid ? scopes : Collections.emptyList();
    }
}
