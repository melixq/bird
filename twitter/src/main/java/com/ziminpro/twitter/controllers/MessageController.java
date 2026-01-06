package com.ziminpro.twitter.controllers;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Message;
import com.ziminpro.twitter.services.MessagesService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessagesService messagesService;

    public MessageController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @GetMapping("/{message-id}")
    public Mono<Message> getMessageById(@PathVariable("message-id") UUID messageId) {
        return messagesService.getMessageById(messageId);
    }

    @GetMapping("/producer/{producer-id}")
    public Mono<List<Message>> getMessagesForProducer(@PathVariable("producer-id") UUID producerId) {
        return messagesService.getMessagesForProducerById(producerId);
    }

    @GetMapping("/my-feed")
    public Mono<List<Message>> getMyFeed(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return messagesService.getMessagesForSubscriberById(userId);
    }

    @PostMapping
    public Mono<UUID> createMessage(
            @RequestBody Message message,
            Authentication authentication) {

        boolean isProducer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUCER"));

        if (!isProducer) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
        }

        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        message.setAuthor(userId);

        return messagesService.createMessage(message);
    }

    @DeleteMapping("/{message-id}")
    public Mono<Void> deleteMessage(@PathVariable("message-id") UUID messageId) {
        messagesService.deleteMessageById(messageId);
        return Mono.empty();
    }
}