package com.showbie.publicservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.showbie.publicservice.models.Message;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Functional tests used to validate public-service behavior.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "auth.token.key=ABC123"
})
class PublicServiceFunctionalTests {

    @Value("${request.host:localhost:8081}")
    private String host;

    @Value("${request.resource:message}")
    private String resource;

    @Value("${auth.token.key}")
    private String authTokenSigningKey;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Test
    void should_return_message_public_service_happy_path() {

        Message message = makeRequestWithScopes("PUBLIC_SERVICE");

        assertThat(message).isNotNull();
        assertThat(message.getPublicText()).isNotNull();
        assertThat(message.getPrivateText()).isNull();
        System.out.println(message.getPublicText());
    }

    @Test
    void should_return_messages_public_and_internal_service_happy_path() {

        Message message = makeRequestWithScopes("PUBLIC_SERVICE", "PRIVATE_SERVICE");

        assertThat(message).isNotNull();
        assertThat(message.getPublicText()).isNotNull();
        assertThat(message.getPrivateText()).isNotNull();
        System.out.println(message.getPublicText());
        System.out.println(message.getPrivateText());
    }

    @Test
    void should_return_not_found_if_invalid_resource() throws JsonProcessingException {

        HttpClientErrorException exception = null;

        try {
            makeValidRequest("invalidResource");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 404, "Not Found", null);
    }

    @Test
    void should_return_401_if_not_authenticated() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        try {
            makeRequestInternal(resource, (String) null); // no tpken supplied, thus no Authorization header
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_authentication_fails_wrong_key() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = createTokenInternal(
                "not_the_token_key", // signed with an unexpected key
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + (60 * 1000)),
                "PUBLIC_SERVICE"
        );
        try {
            makeRequestInternal(resource, token);
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_authentication_fails_wrong_scope() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = createToken(
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + (60 * 1000)),
                "OTHER_SERVICE" // unsupported scope
        );
        try {
            makeRequestInternal(resource, token);
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_authentication_fails_expired() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = createToken(
                new Date(System.currentTimeMillis() - (60000)),
                new Date(System.currentTimeMillis() - (30000)), // expires in the past
                "PUBLIC_SERVICE"
        );
        try {
            makeRequestInternal(resource, token);
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_authentication_missing_expires() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = createToken(
                new Date(System.currentTimeMillis() - (60000)),
                null, // missing expiresAt
                "PUBLIC_SERVICE"
        );
        try {
            makeRequestInternal(resource, token);
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_authentication_missing_issuedAt() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = createToken(
                null, // missing issuedAt
                new Date(System.currentTimeMillis() + (60000)),
                "PUBLIC_SERVICE"
        );
        try {
            makeRequestInternal(resource, token);
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_invalid_authorization_header() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = createToken(
                new Date(System.currentTimeMillis() - (60000)),
                new Date(System.currentTimeMillis() + (60000)),
                "PUBLIC_SERVICE"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CorrelationId", UUID.randomUUID().toString());
        headers.set("Authorization", "Bear " + token); // not a correct authorization header
        try {
            makeRequestInternal(resource, headers);
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    private Message makeValidRequest() {
        return makeValidRequest(resource);
    }

    private Message makeValidRequest(String resource) {
        String token = createToken("PUBLIC_SERVICE", "PRIVATE_SERVICE");
        return makeRequestInternal(resource, token);
    }

    private Message makeRequestWithScopes(String... scope) {
        String token = createToken(scope);
        return makeRequest(token);
    }

    private Message makeRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CorrelationId", UUID.randomUUID().toString());
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return makeRequestInternal(resource, headers);
    }

    private Message makeRequestInternal(String resource, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CorrelationId", UUID.randomUUID().toString());
        if (token != null) {
            headers.setBearerAuth(token);
        }

        return makeRequestInternal(resource, headers);
    }

    private Message makeRequestInternal(String resource, HttpHeaders headers) {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = String.format("http://%s/%s", host, resource);
        ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                Message.class);
        return response.getBody();
    }

    private String createToken(String... scope) {
        return createTokenInternal(
                authTokenSigningKey,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + (60 * 1000)),
                scope
        );
    }

    private String createToken(Date issuedAt, Date expiresAt, String... scope) {
        return createTokenInternal(authTokenSigningKey, issuedAt, expiresAt, scope);
    }

    private String createTokenInternal(String tokenKey, Date issuedAt, Date expiresAt, String... scope) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("scopes", Arrays.stream(scope).distinct().collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(SignatureAlgorithm.HS256, tokenKey)
                .compact();
    }

    private void assertClientError(HttpClientErrorException ex, int expectedStatus, String expectedErrorSubstring, String expectedMessageSubstring) throws JsonProcessingException {
        assertThat(ex).isNotNull();
        String responseBody = ex.getResponseBodyAsString();
        assertThat(responseBody).isNotNull();
        Map<String, Object> json = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
        });
        System.out.println(json);

        Integer status = (Integer) json.get("status");
        assertThat(status).isNotNull();
        assertThat(status).isEqualTo(expectedStatus);
        assertThat(ex.getRawStatusCode()).isEqualTo(expectedStatus);

        String error = (String) json.get("error");
        assertThat(error).isNotNull();
        if (expectedErrorSubstring != null) {
            assertThat(error.contains(expectedErrorSubstring)).isTrue();
        }

        String message = (String) json.get("message");
        assertThat(message).isNotNull();
        if (expectedMessageSubstring != null) {
            assertThat(message.contains(expectedMessageSubstring)).isTrue();
        }

        String timestamp = (String) json.get("timestamp");
        // good enough that timestamp is a non-empty string
        assertThat(timestamp).isNotNull();
        assertThat(timestamp.isEmpty()).isFalse();
    }
}
