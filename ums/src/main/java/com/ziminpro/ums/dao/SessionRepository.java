package com.ziminpro.ums.dao;

import com.ziminpro.ums.dtos.Session;

import java.util.UUID;

public interface SessionRepository {
    UUID createSession(Session session);
    Session findSessionByAccessToken(String accessToken);
    Session findSessionByRefreshToken(String refreshToken);
    Session findActiveSessionByUserId(UUID userId);
    boolean updateSessionAccessToken(UUID sessionId, String newAccessToken, Long expiresAt);
    boolean deactivateSession(UUID sessionId);
    boolean deactivateAllUserSessions(UUID userId);
    void cleanupExpiredSessions();
}
