//package com.showbie.publicservice.http.security;
//
//import io.jsonwebtoken.JwtBuilder;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import org.junit.jupiter.api.Test;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//public class TokenParserTest {
//
//    private String signingKey = "password";
//
//    @Test
//    public void knownScope_success() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isTrue();
//        assertThat(parser.getScopes().size()).isEqualTo(1);
//        assertThat(parser.getScopes().contains("PUBLIC_SERVICE")).isTrue();
//    }
//
//    @Test
//    public void knownScopes_success() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                Arrays.asList("PUBLIC_SERVICE", "PRIVATE_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isTrue();
//        assertThat(parser.getScopes().size()).isEqualTo(2);
//        assertThat(parser.getScopes().contains("PUBLIC_SERVICE")).isTrue();
//        assertThat(parser.getScopes().contains("PRIVATE_SERVICE")).isTrue();
//    }
//
//    @Test
//    public void missingScopes_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                null // no scopes
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void unknownScopes_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                Arrays.asList("UNKNOWN_A", "UNKNOWN_B") // all unsupported scopes
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void mixOfKnownAndUnknownScopes_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                Arrays.asList("PUBLIC_SERVICE", "PRIVATE_SERVICE", "UNKNOWN") // includes unsupported scope
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void zeroScopesSupplied_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                Collections.emptyList() // empty scopes
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void missingIssuedAt_failure() {
//        // arrange
//        String token = createToken(
//                null, // no issuedAt
//                new Date(System.currentTimeMillis() + 60000),
//                signingKey,
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void issuedAtInFuture_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() + 60000), // in the future
//                new Date(System.currentTimeMillis() + 120000),
//                signingKey,
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void missingExpires_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                null, // no expiresAt
//                signingKey,
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void expiresInPast_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() - 50000), // before now
//                signingKey,
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void expiresBeforeIssuedAt_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() + 60000),
//                new Date(System.currentTimeMillis() - 60000), // before the issuedAt
//                signingKey,
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void notSigned_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                null, // do not sign
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void signedWithDifferentKey_failure() {
//        // arrange
//        String token = createToken(
//                new Date(System.currentTimeMillis() - 60000),
//                new Date(System.currentTimeMillis() + 60000),
//                "123456", // unexpected signing key
//                Collections.singletonList("PUBLIC_SERVICE")
//        );
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void garbageToken_failure() {
//        // arrange
//        String token = "ABCDEFGHIJKLMNOPQRSTUVWXYZ.0123456789";
//        System.out.println(token);
//
//        // act
//        TokenParser parser = new TokenParser(signingKey, token);
//
//        // assert
//        assertThat(parser.isValid()).isFalse();
//        assertThat(parser.getScopes().size()).isEqualTo(0);
//    }
//
//    String createToken(Date issuedAt, Date expiresBy, String signingKey, List<String> scopes) {
//        JwtBuilder builder = Jwts.builder();
//        if (scopes != null) {
//            Map<String, Object> claims = new HashMap<>();
//            claims.put("scopes", scopes.stream().distinct().collect(Collectors.toList()));
//            builder.setClaims(claims);
//        }
//        if (issuedAt != null) {
//            builder.setIssuedAt(issuedAt);
//        }
//        if (expiresBy != null) {
//            builder.setExpiration(expiresBy);
//        }
//        if (signingKey != null) {
//            builder.signWith(SignatureAlgorithm.HS256, signingKey);
//        }
//        return builder.compact();
//    }
//}
