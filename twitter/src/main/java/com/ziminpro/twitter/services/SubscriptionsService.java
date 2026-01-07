package com.ziminpro.twitter.services;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dao.SubscriptionRepository;
import com.ziminpro.twitter.dtos.Subscription;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class SubscriptionsService {
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionsService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Mono<Subscription> getSubscriptions(UUID subscriberId) {
        return Mono.fromCallable(() -> {
            Subscription subscription = subscriptionRepository.getSubscription(subscriberId);
            if (subscription.getSubscriber() == null) {
                // Return empty subscription if user has no subscriptions
                subscription = new Subscription();
                subscription.setSubscriber(subscriberId);
            }
            return subscription;
        });
    }

    public Mono<UUID> createSubscriptions(
            Subscription subscription,
            UUID userId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        boolean isSubscriber = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUBSCRIBER"));

        if (!isSubscriber) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with SUBSCRIBER role can subscribe"));
        }

        subscription.setSubscriber(userId);
        return Mono.fromCallable(() -> {
            UUID id = subscriptionRepository.createSubscription(subscription);
            if (id == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Subscription creation failed");
            }
            return id;
        });
    }

    public Mono<UUID> updateSubscriptions(
            Subscription subscription,
            UUID userId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        boolean isSubscriber = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUBSCRIBER"));

        if (!isSubscriber) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with SUBSCRIBER role can update subscriptions"));
        }

        subscription.setSubscriber(userId);
        return Mono.fromCallable(() -> {
            UUID id = subscriptionRepository.updateSubscription(subscription);
            if (id == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Subscription update failed");
            }
            return id;
        });
    }

    public Mono<Void> deleteAllSubscriptions(
            UUID subscriberId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        boolean isSubscriber = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUBSCRIBER"));

        if (!isSubscriber) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with SUBSCRIBER role can manage subscriptions"));
        }

        return Mono.fromRunnable(() -> {
            if (!subscriptionRepository.deleteSubscription(subscriberId)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Deletion of all subscriptions failed");
            }
        });
    }

    public Mono<Void> deleteSingleSubscription(
            UUID subscriberId,
            UUID producerId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        boolean isSubscriber = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUBSCRIBER"));

        if (!isSubscriber) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with SUBSCRIBER role can manage subscriptions"));
        }

        return Mono.fromRunnable(() -> {
            if (!subscriptionRepository.deleteSingleSubscription(subscriberId, producerId)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Subscription deletion failed");
            }
        });
    }
}
