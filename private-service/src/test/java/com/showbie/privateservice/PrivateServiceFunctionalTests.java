package com.showbie.privateservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.showbie.common.http.security.TokenGenerator;
import com.showbie.common.models.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Functional tests used to validate private-service behavior.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "auth.token.key=ABC123" // required property
})
class PrivateServiceFunctionalTests {

    @LocalServerPort
    private int port;

    @Value("${request.host:localhost}")
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

        Message message = makeRequestWithScopes("PRIVATE_SERVICE");

        assertThat(message).isNotNull();
        assertThat(message.getText()).isNotNull();
        assertThat(message.getOrigin()).isNotNull();
        assertThat(message.getOrigin()).isEqualTo("private");
        System.out.println(message);

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
            makeRequestInternal(resource, (String) null); // no token supplied, thus no Authorization header
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    @Test
    void should_return_401_if_authentication_fails_wrong_key() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        String token = TokenGenerator.createTokenHS256(
                "not_the_token_key", // signed with an unexpected key
                5000,
                "PRIVATE_SERVICE"
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
        String token = TokenGenerator.createTokenHS256(
                authTokenSigningKey,
                5000,
                "PUBLIC_SERVICE" // unsupported scope
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
        String token = TokenGenerator.createTokenHS256(
                authTokenSigningKey,
                new Date(System.currentTimeMillis() - (60000)),
                new Date(System.currentTimeMillis() - (30000)), // expires in the past
                "PRIVATE_SERVICE"
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
        String token = TokenGenerator.createTokenHS256(
                authTokenSigningKey,
                5000,
                "PRIVATE_SERVICE"
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

    private Message makeValidRequest(String resource) {
        String token = TokenGenerator.createTokenHS256(
                authTokenSigningKey,
                5000,
                "PRIVATE_SERVICE"
        );
        return makeRequestInternal(resource, token);
    }

    private Message makeRequestWithScopes(String... scope) {
        String token = TokenGenerator.createTokenHS256(
                authTokenSigningKey,
                5000,
                scope
        );
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
        String url = String.format("http://%s:%d/%s", host, port, resource);
        ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                Message.class);
        return response.getBody();
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
