package com.showbie.sharedcode.http.correlation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Request filter to extract the correlation id from the incoming request and
 * store it for subsequent outgoing requests. This allows us to observe via
 * logging incoming requests as they travel through our services.
 */
@Component
public class CorrelationRequestFilter extends OncePerRequestFilter {
    public static String CORRELATION_ID_HEADER = "X-CorrelationId";
    public static String CORRELATION_MDC_KEY = "correlationId";

    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // store the request's correlation id in the logging subsystem's MDC
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasLength(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            logger.warn("Request correlation id not supplied, using {}", correlationId);
        }
        MDC.put(CORRELATION_MDC_KEY, correlationId);

        // let other filters execute
        filterChain.doFilter(request, response);
    }
}
