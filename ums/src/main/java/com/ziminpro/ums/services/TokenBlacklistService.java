package com.ziminpro.ums.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    public TokenBlacklistService() {
        ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    public boolean blacklistToken(String jti, long expiresAt) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        long now = System.currentTimeMillis();
        long ttl = expiresAt - now;

        if (ttl > 0) {
            tokenBlacklist.put(jti, expiresAt);
            return true;
        }

        return false;
    }

    public boolean isTokenBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        Long expiresAt = tokenBlacklist.get(jti);
        if (expiresAt == null) {
            return false;
        }

        if (expiresAt > System.currentTimeMillis()) {
            return true;
        } else {
            tokenBlacklist.remove(jti);
            return false;
        }
    }

    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    public int getBlacklistSize() {
        return tokenBlacklist.size();
    }
}
