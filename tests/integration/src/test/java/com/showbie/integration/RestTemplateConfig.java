package com.showbie.sharedlibrary.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

/**
 * Common RestTemplate configuration.
 */
@Configuration
public class RestTemplateConfig {

    @Value("${http.timeout:5000}")
    private long requestTimeout;

    /**
     * RestTemplate builder. Configures the request timeout and injects an interceptor to
     * handle correlation ids.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        return builder
                .setConnectTimeout(Duration.ofMillis(requestTimeout))
                .setReadTimeout(Duration.ofMillis(requestTimeout))
                .build();
    }

}