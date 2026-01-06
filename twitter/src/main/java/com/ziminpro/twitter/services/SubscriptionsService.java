package com.ziminpro.twitter.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dao.SubscriptionRepository;
import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.HttpResponseExtractor;
import com.ziminpro.twitter.dtos.Roles;
import com.ziminpro.twitter.dtos.Subscription;
import com.ziminpro.twitter.dtos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class SubscriptionsService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionsService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Mono<Subscription> getSubscriptions(UUID subscriberId) {
        return Mono.fromCallable(() -> subscriptionRepository.getSubscription(subscriberId));
    }

    public void createSubscription(Subscription subscription) {
        if (!subscriptionRepository.createSubscription(subscription)) {
            throw new IllegalStateException("Subscription creation failed");
        }
    }

    public void deleteSubscription(UUID subscriberId) {
        subscriptionRepository.deleteSubscription(subscriberId);
    }
}
