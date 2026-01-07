package com.ziminpro.twitter.dao;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Message;

public interface MessageRepository {
    Message getMessageById(UUID messageId);
    List<Message> getMessagesForProducerById(UUID producerId);
    List<Message> getMessagesForSubscriberById(UUID subscriberId);
    UUID createMessage(Message message);
    int deleteMessageById(UUID messageId);
}
