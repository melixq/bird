package com.ziminpro.twitter.controllers;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dtos.ApiResponse;
import com.ziminpro.twitter.dtos.Subscription;
import com.ziminpro.twitter.services.SubscriptionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

// TODO:
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
    public Mono<ResponseEntity<ApiResponse<Subscription>>> getMySubscriptions(Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.getSubscriptions(subscriberId)
                .map(subscription -> ResponseEntity.ok(ApiResponse.success(subscription)))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve subscriptions")));
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> createSubscriptions(@RequestBody Subscription subscription, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.createSubscriptions(subscription, subscriberId, authentication.getAuthorities())
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success(null))))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to create subscriptions")));
    }

    @PutMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> updateSubscriptions(@RequestBody Subscription subscription, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.updateSubscriptions(subscription, subscriberId, authentication.getAuthorities())
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success(null))))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to update subscriptions")));
    }

    @DeleteMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> deleteAllSubscriptions(Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.deleteAllSubscriptions(subscriberId, authentication.getAuthorities())
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success(null))))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete all subscriptions")));
    }

    @PostMapping("/producer/{producer-id}")
    public Mono<ResponseEntity<ApiResponse<Object>>> subscribeToProducer(@PathVariable("producer-id") UUID producerId, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriberId);
        subscription.setProducers(List.of(producerId));
        return subscriptionsService.createSubscriptions(subscription, subscriberId, authentication.getAuthorities())
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success(null))))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to subscribe to producer")));
    }

    @DeleteMapping("/producer/{producer-id}")
    public Mono<ResponseEntity<ApiResponse<Object>>> unsubscribeFromProducer(@PathVariable("producer-id") UUID producerId, Authentication authentication) {
        UUID subscriberId = UUID.fromString(authentication.getPrincipal().toString());
        return subscriptionsService.deleteSingleSubscription(subscriberId, producerId, authentication.getAuthorities())
                .then(Mono.just(ResponseEntity.ok(ApiResponse.success(null))))
                .onErrorReturn(ResponseEntity.badRequest().body(ApiResponse.error("Failed to unsubscribe from producer")));
    }
}
