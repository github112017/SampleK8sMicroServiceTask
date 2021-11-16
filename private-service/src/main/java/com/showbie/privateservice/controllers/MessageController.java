package com.showbie.privateservice.controllers;

import com.showbie.common.models.InternalMessage;
import com.showbie.common.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    Logger logger = LoggerFactory.getLogger(MessageController.class);

    private MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/message")
    public InternalMessage message() {

        logger.info("Starting message request");

        InternalMessage result = new InternalMessage(messageService.getPhrase());

        logger.info("Completed message request");

        return result;
    }
}



