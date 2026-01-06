package com.ziminpro.twitter.services;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dao.MessageRepository;
import com.ziminpro.twitter.dtos.Message;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class MessagesService {
    private final MessageRepository messageRepository;

    public MessagesService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Mono<UUID> createMessage(Message message) {
        return Mono.fromCallable(() -> {
            UUID id = messageRepository.createMessage(message);
            if (id == null) {
                throw new IllegalStateException("Message creation failed");
            }
            return id;
        });
    }

    public Mono<Message> getMessageById(UUID messageId) {
        return Mono.fromCallable(() -> messageRepository.getMessagebyId(messageId));
    }

    public Mono<List<Message>> getMessagesForProducerById(UUID producerId) {
        return Mono.fromCallable(() -> messageRepository.getMessagesForProducerById(producerId));
    }

    public Mono<List<Message>> getMessagesForSubscriberById(UUID subscriberId) {
        return Mono.fromCallable(() -> messageRepository.getMessagesForSubscriberById(subscriberId));
    }

    public void deleteMessageById(UUID messageId) {
        messageRepository.deleteMessageById(messageId);
    }
}
