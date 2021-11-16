package com.showbie.integration;

import com.showbie.integration.models.Message;
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

/**
 * Integration tests used to validate plumbing when services are deployed to Kubernetes.
 */
@SpringBootTest
class ApplicationIntegrationTests {

	@Value("${request.host}")
	private String host;

	@Value("${request.resource:message}")
	private String resource;

	@Value("${auth.token.key}")
	private String authTokenSigningKey;

	private RestTemplate restTemplate;

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Test
	void should_return_public_message_happy_path() {

		Message message = makeRequestPublicOnly();

		assertThat(message).isNotNull();
		assertThat(message.getPublicText()).isNotNull();
		System.out.println("PUBLIC_MESSAGE: " + message.getPublicText());
		assertThat(message.getPrivateText()).isNull();
	}

	@Test
	void should_return_public_and_private_messages_happy_path() {

		Message message = makeRequestPublicAndPrivate();

		assertThat(message).isNotNull();
		assertThat(message.getPublicText()).isNotNull();
		System.out.println("PUBLIC_MESSAGE:  " + message.getPublicText());
		assertThat(message.getPrivateText()).isNotNull();
		System.out.println("PRIVATE_MESSAGE: " + message.getPrivateText());
	}

	@Test
	void should_return_not_found_if_invalid_resource() {

		String token = generateToken("PUBLIC_SERVICE");
		HttpClientErrorException exception = null;

		try {
			makeRequestInternal("invalidResource", token);
		} catch (HttpClientErrorException ex) {
			exception = ex;
		}

		assertThat(exception).isNotNull();
		assertThat(exception.getStatusCode().value()).isEqualTo(404);
	}

	@Test
	void should_return_unauthorized_if_invalid_token() {

		String token = generateToken("UNKNOWN_SCOPE");
		HttpClientErrorException exception = null;

		try {
			makeRequestInternal(resource, token);
		} catch (HttpClientErrorException ex) {
			exception = ex;
		}

		assertThat(exception).isNotNull();
		assertThat(exception.getStatusCode().value()).isEqualTo(401);
	}

	private Message makeRequestPublicOnly() {
		String token = generateToken("PUBLIC_SERVICE");
		return makeRequestInternal(resource, token);
	}

	private Message makeRequestPublicAndPrivate() {
		String token = generateToken("PUBLIC_SERVICE", "PRIVATE_SERVICE");
		return makeRequestInternal(resource, token);
	}

	private Message makeRequestInternal(String resource, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("X-CorrelationId", UUID.randomUUID().toString());
		headers.setBearerAuth(token);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		String url = String.format("http://%s/%s", host, resource);
		ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET, entity,
			Message.class);
		return response.getBody();
	}

	private String generateToken(String... scope) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("scopes", Arrays.stream(scope).distinct().collect(Collectors.toList()));
		long nowMillis = System.currentTimeMillis();
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(new Date(nowMillis))
				.setExpiration(new Date(nowMillis + (5 * 60 * 1000))) // 5 min from now
				.signWith(SignatureAlgorithm.HS256, authTokenSigningKey)
				.compact();
	}
}
