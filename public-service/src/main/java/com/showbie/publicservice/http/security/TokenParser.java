package com.showbie.publicservice.http.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to parse and validate a JWT token.
 */
public class TokenParser {
    Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean isValid;
    private final String principle;
    private final List<String> scopes;

    public TokenParser(String token) {
        // TODO: implement token parser

        isValid = true;
        principle = "farren.layton@gmail.com";
        scopes = Arrays.asList("A", "B", "C");
    }

    public boolean isValid() {
        return isValid;
    }

    public String getPrinciple() {
        return principle;
    }

    public List<String> getScopes() {
        return scopes;
    }
}
