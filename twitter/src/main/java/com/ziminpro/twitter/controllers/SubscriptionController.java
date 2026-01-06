package com.ziminpro.twitter.controllers;

import java.util.UUID;

import com.ziminpro.twitter.dtos.Subscription;
import com.ziminpro.twitter.services.SubscriptionsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final SubscriptionsService subscriptionsService;

    public SubscriptionController(SubscriptionsService subscriptionsService) {
        this.subscriptionsService = subscriptionsService;
    }

    @GetMapping
    public Mono<Subscription> getMySubscriptions(Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.getSubscriptions(subscriberId);
    }

    @PostMapping
    public Mono<Void> createSubscription(
            @RequestBody Subscription subscription,
            Authentication authentication) {

        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        subscription.setSubscriber(subscriberId);

        subscriptionsService.createSubscription(subscription);
        return Mono.empty();
    }

    @DeleteMapping
    public Mono<Void> deleteSubscription(Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        subscriptionsService.deleteSubscription(subscriberId);
        return Mono.empty();
    }
}
