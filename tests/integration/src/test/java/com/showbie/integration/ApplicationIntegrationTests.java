package com.showbie.integration;

import com.showbie.integration.models.Message;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Integration tests used to validate plumbing when services are deployed together.
 *
 * We can assume that the functional tests have already verified correct behaviors of
 * each service, here we are more interested in validating the correctness of the
 * service configuration within a deployment environment. These tests should work
 * when the services are running locally (ie. gradle bootrun) or when deployed to a
 * Kubernetes cluster.
 */
@SpringBootTest
class ApplicationIntegrationTests {
	Logger logger = LoggerFactory.getLogger(getClass());

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
	void _showInfo() { // TODO: REMOVE AFTER TESTING
		System.out.println("http://" + host + "/" + resource);
		System.out.println(authTokenSigningKey);
	}

	@Test
	void should_return_public_message_happy_path() {

		List<Message> messages = makeRequestPublicOnly();

		assertThat(messages).isNotNull();
		messages.forEach(m -> logger.info("--> {}", m));
		assertThat(messages.size()).isEqualTo(1);
		assertThat(messages.get(0).getOrigin()).isEqualTo("public");
		assertThat(messages.get(0).getText()).isNotNull();
	}

	@Test
	void should_return_private_message_happy_path() {

		List<Message> messages = makeRequestPrivateOnly();

		assertThat(messages).isNotNull();
		messages.forEach(m -> logger.info("--> {}", m));
		assertThat(messages.size()).isEqualTo(1);
		assertThat(messages.get(0).getOrigin()).isEqualTo("private");
		assertThat(messages.get(0).getText()).isNotNull();
	}

	@Test
	void should_return_public_and_private_messages_happy_path() {

		List<Message> messages = makeRequestPublicAndPrivate();

		assertThat(messages).isNotNull();
		messages.forEach(m -> logger.info("--> {}", m));
		assertThat(messages.size()).isEqualTo(2);
		assertThat(
				messages.stream().anyMatch(m -> m.getOrigin().equals("public") && m.getText() != null)
		).isTrue();
		assertThat(
				messages.stream().anyMatch(m -> m.getOrigin().equals("private") && m.getText() != null)
		).isTrue();
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
		logger.error("--> {}", exception.getMessage());
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
		logger.error("--> {}", exception.getMessage());
		assertThat(exception.getStatusCode().value()).isEqualTo(401);
	}

	private List<Message> makeRequestPublicOnly() {
		String token = generateToken("PUBLIC_SERVICE");
		return makeRequestInternal(resource, token);
	}

	private List<Message> makeRequestPrivateOnly() {
		String token = generateToken("PRIVATE_SERVICE");
		return makeRequestInternal(resource, token);
	}

	private List<Message> makeRequestPublicAndPrivate() {
		String token = generateToken("PUBLIC_SERVICE", "PRIVATE_SERVICE");
		return makeRequestInternal(resource, token);
	}

	private List<Message> makeRequestInternal(String resource, String token) {
		String correlationId = UUID.randomUUID().toString();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("X-CorrelationId", correlationId);
		headers.setBearerAuth(token);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		String url = String.format("http://%s/%s", host, resource);
		logger.info("Requesting {} with correlationId={}", url, correlationId);
		ResponseEntity<Message[]> response = restTemplate.exchange(url, HttpMethod.GET, entity,
			Message[].class);
		return Arrays.asList(Objects.requireNonNull(response.getBody()));
	}

	private String generateToken(String... scope) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("scopes", Arrays.stream(scope).distinct().collect(Collectors.toList()));
		long nowMillis = System.currentTimeMillis();
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(new Date(nowMillis))
				.setExpiration(new Date(nowMillis + (5 * 60 * 1000))) // 5 min from now
				.signWith(SignatureAlgorithm.HS256, authTokenSigningKey.getBytes())
				.compact();
	}
}
