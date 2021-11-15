package com.showbie.integration;

import com.showbie.integration.models.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ApplicationIntegrationTests {

	@Value("${request.host}")
	private String host;

	@Value("${request.resource:message}")
	private String resource;

	private RestTemplate restTemplate;

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Test
	void should_return_message_happy_path() {

		Message message = makeRequest();

		assertThat(message).isNotNull();
		assertThat(message.getText()).isNotNull();
	}

	@Test
	void should_return_not_found_if_invalid_resource() {

		HttpClientErrorException exception = null;

		try {
			makeRequestInternal("invalidResource");
		} catch (HttpClientErrorException ex) {
			exception = ex;
		}

		assertThat(exception).isNotNull();
		assertThat(exception.getStatusCode().value()).isEqualTo(404);
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

}
