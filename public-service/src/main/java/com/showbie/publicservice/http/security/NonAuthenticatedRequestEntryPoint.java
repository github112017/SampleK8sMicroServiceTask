//package com.showbie.publicservice.http.security;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.Serializable;
//
///**
// * Configuration to ensure that a request authentication failure responds with a 401 response.
// */
//@Component
//public class NonAuthenticatedRequestEntryPoint implements AuthenticationEntryPoint, Serializable {
//    Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
//        logger.error(
//                "Unauthenticated request from {} to {}: {}",
//                request.getRemoteHost(),
//                request.getRequestURI(),
//                authException.getMessage()
//        );
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401
//    }
//}
