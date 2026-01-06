package com.ziminpro.twitter.services;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dao.MessageRepository;
import com.ziminpro.twitter.dtos.Message;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class MessagesService {
    private final MessageRepository messageRepository;

    public MessagesService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Mono<UUID> createMessage(
            Message message,
            UUID userId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        boolean isProducer = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PRODUCER"));

        if (!isProducer) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only producers can create messages"));
        }

        message.setAuthor(userId);
        return Mono.fromCallable(() -> {
            UUID id = messageRepository.createMessage(message);
            if (id == null) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Message creation failed");
            return id;
        });
    }

    public Mono<Message> getMessageById(UUID messageId) {
        return Mono.fromCallable(() -> {
            Message message = messageRepository.getMessagebyId(messageId);
            if (message.getId() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
            }
            return message;
        });
    }

    public Mono<List<Message>> getMessagesForProducerById(UUID producerId) {
        return Mono.fromCallable(() -> messageRepository.getMessagesForProducerById(producerId));
    }

    public Mono<List<Message>> getMessagesForSubscriberById(UUID subscriberId) {
        return Mono.fromCallable(() -> messageRepository.getMessagesForSubscriberById(subscriberId));
    }

    public Mono<Void> deleteMessageById(
            UUID messageId,
            UUID requesterId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        return Mono.fromRunnable(() -> {
            Message message = messageRepository.getMessagebyId(messageId);
            if (message.getId() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
            }

            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = message.getAuthor().equals(requesterId);

            if (!isAdmin && !isOwner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Administrators can delete messages of other users");
            }

            int deleted = messageRepository.deleteMessageById(messageId);
            if (deleted != 1) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Message deletion failed");
            }
        });
    }
}
