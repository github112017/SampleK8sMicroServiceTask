package com.showbie.common.http.correlation;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.showbie.common.http.security.TokenValidationFilter.CORRELATION_ID_HEADER;
import static com.showbie.common.http.security.TokenValidationFilter.CORRELATION_MDC_KEY;

/**
 * Intercepts outgoing requests to populate the X-CorrelationId header with
 * that from the original incoming request. This allows us to observe via
 * logging incoming requests as they travel through our services.
 * See also {@link com.showbie.common.http.security.TokenValidationFilter}
 */
@Component
public class RestTemplateCorrelationInterceptor implements ClientHttpRequestInterceptor {

    /**
     * Adds the stored correlation id to the request headers.
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {

        // DOC - if available, the correlation is stored in the logging subsystem's MDC
        String correlationId = MDC.get(CORRELATION_MDC_KEY);
        if (correlationId != null) {
            request.getHeaders().add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_MDC_KEY));
        }
        return execution.execute(request, body);
    }
}

