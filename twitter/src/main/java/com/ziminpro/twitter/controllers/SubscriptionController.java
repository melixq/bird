package com.ziminpro.twitter.controllers;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Subscription;
import com.ziminpro.twitter.services.SubscriptionsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

// TODO:
// - Correct Subs. Authorities and etc
// - Fix unability to call UMS endpoints outside browser
// - Update UI
// - Store avatar url in JWT


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
    public Mono<Void> createSubscriptions(@RequestBody Subscription subscription, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.createSubscriptions(subscription, subscriberId, authentication.getAuthorities())
                .then();
    }

    @PutMapping
    public Mono<Void> updateSubscriptions(@RequestBody Subscription subscription, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.updateSubscriptions(subscription, subscriberId, authentication.getAuthorities())
                .then();
    }

    @DeleteMapping
    public Mono<Void> deleteAllSubscriptions(Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.deleteAllSubscriptions(subscriberId, authentication.getAuthorities());
    }

    @PostMapping("/producer/{producer-id}")
    public Mono<Void> subscribeToProducer(@PathVariable("producer-id") UUID producerId, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriberId);
        subscription.setProducers(List.of(producerId));
        return subscriptionsService.createSubscriptions(subscription, subscriberId, authentication.getAuthorities())
                .then();
    }

    @DeleteMapping("/producer/{producer-id}")
    public Mono<Void> unsubscribeFromProducer(@PathVariable("producer-id") UUID producerId, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.deleteSingleSubscription(subscriberId, producerId, authentication.getAuthorities());
    }
}
