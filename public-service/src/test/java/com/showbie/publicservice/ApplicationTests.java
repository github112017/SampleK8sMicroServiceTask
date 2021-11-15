package com.showbie.publicservice;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.showbie.publicservice.models.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ApplicationTests {

    @Value("${request.host:localhost:8081}")
    private String host;

    @Value("${request.resource:message}")
    private String resource;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    @Test
    void should_return_message_happy_path() {

        Message message = makeRequest();

        assertThat(message).isNotNull();
        assertThat(message.getText()).isNotNull();
        System.out.println(message.getText());
    }

    @Test
    void should_return_not_found_if_invalid_resource() throws JsonProcessingException {

        HttpClientErrorException exception = null;

        try {
            makeRequestInternal("invalidResource");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 404, "Not Found", null);
    }

    @Test
    void should_return_401_if_not_authenticated() throws JsonProcessingException {
        HttpClientErrorException exception = null;
        try {
            makeRequest();
            fail("should not be reached");
        } catch (HttpClientErrorException ex) {
            exception = ex;
        }

        assertClientError(exception, 401, "Unauthorized", "Authentication is required");
    }

    private Message makeRequest() {
        return makeRequestInternal(resource);
    }

    private Message makeRequestInternal(String resource) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-CorrelationId", UUID.randomUUID().toString());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = String.format("http://%s/%s", host, resource);
        ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                Message.class);
        return response.getBody();
    }

    private void assertClientError(HttpClientErrorException ex, int expectedStatus, String expectedErrorSubstring, String expectedMessageSubstring) throws JsonProcessingException {
        assertThat(ex).isNotNull();
        String responseBody = ex.getResponseBodyAsString();
        assertThat(responseBody).isNotNull();
        Map<String,Object> json = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
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
