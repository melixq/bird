package com.ziminpro.twitter.controllers;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dtos.ApiResponse;
import com.ziminpro.twitter.dtos.Message;
import com.ziminpro.twitter.services.MessagesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessagesService messagesService;

    public MessageController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @GetMapping("/{message-id}")
    public Mono<ResponseEntity<ApiResponse<Message>>> getMessageById(@PathVariable("message-id") UUID messageId) {
        return messagesService.getMessageById(messageId)
                .map(message -> ResponseEntity.ok(ApiResponse.success(message)))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve message")));
    }

    @GetMapping("/producer/{producer-id}")
    public Mono<ResponseEntity<ApiResponse<List<Message>>>> getMessagesForProducer(@PathVariable("producer-id") UUID producerId) {
        return messagesService.getMessagesForProducerById(producerId)
                .map(messages -> ResponseEntity.ok(ApiResponse.success(messages)))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve messages for producer")));
    }

    @GetMapping("/my-feed")
    public Mono<ResponseEntity<ApiResponse<List<Message>>>> getMyFeed(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return messagesService.getMessagesForSubscriberById(userId)
                .map(messages -> ResponseEntity.ok(ApiResponse.success(messages)))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve your feed")));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<UUID>>> createMessage(@RequestBody Message message, Authentication authentication) {
        UUID authorId = UUID.fromString(authentication.getPrincipal().toString());
        return messagesService.createMessage(message, authorId, authentication.getAuthorities())
                .map(id -> ResponseEntity.ok(ApiResponse.success(id)))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to create message")));
    }

    @DeleteMapping("/{message-id}")
    public Mono<ResponseEntity<ApiResponse<Object>>> deleteMessage(@PathVariable("message-id") UUID messageId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return messagesService.deleteMessageById(messageId, userId, authentication.getAuthorities())
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success(null))))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete message")));
    }
}