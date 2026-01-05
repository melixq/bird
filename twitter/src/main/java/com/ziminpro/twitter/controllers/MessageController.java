package com.ziminpro.twitter.controllers;

import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Message;
import com.ziminpro.twitter.services.MessagesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class MessageController {
    private final MessagesService messages;

    public MessageController(MessagesService messages) {
        this.messages = messages;
    }

    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_MESSAGE + "/{message-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessageById(
            @PathVariable(value = "message-id") String messageId) {
        return messages.getMessagebyId(UUID.fromString(messageId));
    }

    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_PRODUCER + "/{producer-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForProducerById(
            @PathVariable(value = "producer-id") String producerId) {
        return messages.getMessagesForProducerById(UUID.fromString(producerId));
    }

    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_SUBSCRIBER + "/{subscriber-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForSubscriberById(
            @PathVariable(value = "subscriber-id") String subscriberId) {
        return messages.getMessagesForSubscriberById(UUID.fromString(subscriberId));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/messages/my-feed")
    public Mono<ResponseEntity<Map<String, Object>>> getMyMessages(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return messages.getMessagesForSubscriberById(UUID.fromString(userId));
    }

    @RequestMapping(method = RequestMethod.POST, path = Constants.URI_MESSAGE, consumes = Constants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, Object>>> createMessage(
            @RequestBody Message message,
            Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        message.setAuthor(UUID.fromString(userId));
        return messages.createMessage(message);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = Constants.URI_MESSAGE + "/{message-id}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteMessageById(
            @PathVariable(value = "message-id") String messageId) {
        return messages.deleteMessageById(UUID.fromString(messageId));
    }
}