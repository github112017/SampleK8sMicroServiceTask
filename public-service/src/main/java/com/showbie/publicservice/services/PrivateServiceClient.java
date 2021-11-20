package com.showbie.publicservice.services;

import com.showbie.common.http.security.TokenGenerator;
import com.showbie.common.models.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * REST client for the private-service.
 */
@Service
public class PrivateServiceClient {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${private.service.host.uri}")       // startup will be halted if this is undefined
    private String hostUri;

    @Value("${private.service.auth.token.key}") // startup will be halted if this is undefined
    private String tokenSigningKey;

    private RestTemplate restTemplate;

    @Autowired
    private void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void showInfo() {
        // TODO - This is provided for debugging purposes only and should be removed before production
        logger.info("DIAGNOSTIC: will call {} using key={}", hostUri, tokenSigningKey);
    }

    public Message getMessage() {
        // DOC - Usually an auth provider is used to obtain a token at some
        //       (small) cost; there it makes sense to cache the token for
        //       reuse until it approaches the expiry time. Here we create
        //       our token so we just make a new one for every request.
        String token = generateShortLivedToken();

        // create required headers (correlation id should be automatically added)
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        // make the request
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Message> response = restTemplate.exchange(
                hostUri,
                HttpMethod.GET,
                entity,
                Message.class
        );
        return response.getBody();
    }

    private String generateShortLivedToken() {
        return TokenGenerator.createTokenHS256(tokenSigningKey, 30000, "PRIVATE_SERVICE");
    }
}
