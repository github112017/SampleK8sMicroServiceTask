package com.showbie.publicservice.controllers;

import com.showbie.common.http.security.AuthenticatedTokenScopes;
import com.showbie.common.models.Message;
import com.showbie.common.services.MessageService;
import com.showbie.publicservice.services.PrivateServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class MessageController {

    Logger logger = LoggerFactory.getLogger(MessageController.class);

    private AuthenticatedTokenScopes authenticatedTokenScopes;
    private MessageService messageService;
    private PrivateServiceClient privateServiceClient;

    @Autowired
    public void setAuthenticatedTokenScopes(AuthenticatedTokenScopes authenticatedTokenScopes) {
        this.authenticatedTokenScopes = authenticatedTokenScopes;
    }

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired
    public void setPrivateServiceClient(PrivateServiceClient privateServiceClient) {
        this.privateServiceClient = privateServiceClient;
    }

    @GetMapping("/message")
    public List<Message> message() {

        logger.info("Starting message request");
        List<Message> result = new ArrayList<>();

        Set<String> scopes = authenticatedTokenScopes.getScopes();

        if (scopes.contains("PUBLIC_SERVICE")) {
	        logger.info("Including public message");
            result.add(new Message(messageService.getPhrase(), "public"));
        }

        if (scopes.contains("PRIVATE_SERVICE")) {
	        logger.info("Including private message");
            result.add(privateServiceClient.getMessage());
        }

        logger.info("Completed message request");

        return result;
    }
}



