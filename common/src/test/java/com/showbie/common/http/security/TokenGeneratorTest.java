package com.showbie.common.http.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TokenGeneratorTest {
    private final String signingKey = "ABC";
    private final List<String> supportedScopes = Arrays.asList("One", "Two", "Three", "Four");


    @Test
    public void createTokenHS256_success() {
        // arrange

        // act
        String token = TokenGenerator.createTokenHS256(signingKey, 5000, "One", "Two", "Three");

        // assert
        assertThat(token).isNotNull();
        TokenParser parser = new TokenParser(signingKey, supportedScopes, token);
        assertThat(parser.isValid()).isTrue();
        assertThat(parser.getScopes()).isEqualTo(Arrays.asList("One", "Two", "Three"));
    }

    @Test
    public void testCreateTokenHS256_withDates_success() {
        // arrange

        // act
        String token = TokenGenerator.createTokenHS256(
                signingKey,
                new Date(System.currentTimeMillis() - 5000),
                new Date(System.currentTimeMillis() + 5000),
                "One", "Two", "Three"
        );

        // assert
        assertThat(token).isNotNull();
        TokenParser parser = new TokenParser(signingKey, supportedScopes, token);
        assertThat(parser.isValid()).isTrue();
        assertThat(parser.getScopes()).isEqualTo(Arrays.asList("One", "Two", "Three"));
    }

    @Test
    public void testCreateTokenHS256_expired_failure() {
        // arrange

        // act
        String token = TokenGenerator.createTokenHS256(
                signingKey,
                new Date(System.currentTimeMillis() - 5000),
                new Date(System.currentTimeMillis() - 5000),
                "One", "Two", "Three"
        );

        // assert
        assertThat(token).isNotNull();
        TokenParser parser = new TokenParser(signingKey, supportedScopes, token);
        assertThat(parser.isValid()).isFalse();
    }

    @Test
    public void testCreateTokenHS256_cannotBeVerifiedWithDifferentKey_failure() {
        // arrange

        // act
        String token = TokenGenerator.createTokenHS256(
                signingKey,
                new Date(System.currentTimeMillis() - 5000),
                new Date(System.currentTimeMillis() - 5000),
                "One", "Two", "Three"
        );

        // assert
        assertThat(token).isNotNull();
        TokenParser parser = new TokenParser("NOT_THE_KEY", supportedScopes, token);
        assertThat(parser.isValid()).isFalse();
    }
}
