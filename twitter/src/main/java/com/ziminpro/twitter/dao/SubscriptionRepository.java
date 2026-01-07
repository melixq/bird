package com.ziminpro.twitter.dao;

import java.util.UUID;

import com.ziminpro.twitter.dtos.Subscription;

public interface SubscriptionRepository {
    Subscription getSubscription(UUID subscriberId);
    UUID createSubscription(Subscription subscription);
    UUID updateSubscription(Subscription subscription);
    boolean deleteSubscription(UUID subscriberId); // Delete all subscriptions for a user
    boolean deleteSingleSubscription(UUID subscriberId, UUID producerId);
}
