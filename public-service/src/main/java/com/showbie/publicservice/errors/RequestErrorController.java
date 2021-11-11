package com.showbie.publicservice.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized error handling for all requests (error responses and controller exceptions)
 *
 * Attempts to transform errors into a common response body for clients.
 */
@RestController
@ControllerAdvice
public class RequestErrorController extends AbstractErrorController {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final String notFoundMessage = "Oops, we can't seem to find what you are looking for";
    private final String internalServerErrorMessage = "Sorry about that, something went wrong on our end";

    public RequestErrorController(final ErrorAttributes errorAttributes) {
        super(errorAttributes, Collections.emptyList());
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * Transforms all errors sent to the "/error" resource into a common client response (failed
     * authentications etc). Any unknown http statues result in an internal server error.
     */
    @ResponseBody
    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> handleError(HttpServletRequest request) {
        HttpStatus status = this.getStatus(request);
        Map<String, Object> attributes = null;
        if (status == HttpStatus.NOT_FOUND) {
            attributes = buildAttributes(new Date(), status, notFoundMessage);
        } else {
            attributes = buildAttributes(new Date(), HttpStatus.INTERNAL_SERVER_ERROR, internalServerErrorMessage);
        }
        return attributes;
    }

    /**
     * Transforms all controller exceptions in to a common client response. Currently ALL controller
     * exceptions are logged and returned to the client as internal server errors.
     */
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleException(RuntimeException ex) {
        logger.error("Service generated an exception: {}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> attributes = buildAttributes(new Date(), status, internalServerErrorMessage);
        return new ResponseEntity<>(attributes, headers, status);
    }

    /**
     * Builds a common set of error attributes to return to clients.
     */
    private Map<String, Object> buildAttributes(Date timestamp, HttpStatus status, String message) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timestamp", timestamp);
        attributes.put("status", status.value());
        attributes.put("error", status.getReasonPhrase());
        attributes.put("message", message);
        return attributes;
    }

}