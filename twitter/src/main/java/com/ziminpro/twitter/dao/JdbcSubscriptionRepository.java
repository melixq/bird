package com.ziminpro.twitter.dao;

import java.util.List;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Subscription;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import static com.ziminpro.twitter.dtos.Constants.DELETE_SINGLE_SUBSCRIPTION;

@Repository
public class JdbcSubscriptionRepository implements SubscriptionRepository {
    private final JdbcTemplate jdbcTemplate;

    private final JdbcMessageRepository jdbcMessageRepository;

    public JdbcSubscriptionRepository(JdbcMessageRepository jdbcMessageRepository, JdbcTemplate jdbcTemplate) {
        this.jdbcMessageRepository = jdbcMessageRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UUID createSubscription(Subscription subscription) {
        if (subscription.getSubscriber() == null || subscription.getProducers() == null
                || subscription.getProducers().isEmpty())
            return null;

        UUID subscriberId = this.createSubscriber(subscription.getSubscriber());

        try {
            for (UUID producerId : subscription.getProducers()) {
                if (jdbcMessageRepository.createProducer(producerId) != null) {
                    jdbcTemplate.update(Constants.CREATE_SUBSCRIPTION,
                            subscription.getSubscriber().toString(),
                            producerId.toString());
                }
            }
        } catch (Exception e) {
            return null;
        }
        return subscriberId;
    }

    @Override
    public UUID updateSubscription(Subscription subscription) {
        this.deleteSubscription(subscription.getSubscriber());
        return this.createSubscription(subscription);
    }

    @Override
    public Subscription getSubscription(UUID subscriberId) {
        List<UUID> producerIds = jdbcTemplate.query(Constants.GET_SUBSCRIPTION,
                (rs, rowNum) -> DaoHelper.bytesArrayToUuid(rs.getBytes("producer_id")),
                subscriberId.toString());

        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriberId);
        subscription.setProducers(producerIds);

        return subscription;
    }

    @Override
    public boolean deleteSubscription(UUID subscriberId) {
        try {
            jdbcTemplate.update(Constants.DELETE_SUBSCRIPTION, subscriberId.toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteSingleSubscription(UUID subscriberId, UUID producerId) {
        try {
            int deleted = jdbcTemplate.update(DELETE_SINGLE_SUBSCRIPTION,
                    subscriberId.toString(), producerId.toString());
            return deleted > 0;
        } catch (Exception e) {
            return false;
        }
    }

    UUID createSubscriber(UUID subscriberId) {
        try {
            jdbcTemplate.update(Constants.CREATE_SUBSCRIBER, subscriberId.toString());
        } catch (Exception e) {
            return null;
        }
        return subscriberId;
    }
}
