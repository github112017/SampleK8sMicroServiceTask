package com.showbie.publicservice.controllers;

import com.showbie.publicservice.models.Message;
import com.showbie.publicservice.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class MessageController {

    Logger logger = LoggerFactory.getLogger(MessageController.class);

    private MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/message")
    public Message message() {

        logger.info("Starting message request");

        Message result = new Message(messageService.getPhrase());

        logger.info("Completed message request");

        return result;
    }
}



