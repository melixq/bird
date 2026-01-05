package com.ziminpro.ums.dao;

import com.ziminpro.ums.dtos.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JdbcSessionRepository implements SessionRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Session> sessionRowMapper = (rs, rowNum) -> Session.builder()
            .id(DaoHelper.bytesArrayToUuid(rs.getBytes("id")))
            .userId(DaoHelper.bytesArrayToUuid(rs.getBytes("user_id")))
            .accessToken(rs.getString("access_token"))
            .refreshToken(rs.getString("refresh_token"))
            .accessTokenExpiresAt(rs.getLong("access_token_expires_at"))
            .refreshTokenExpiresAt(rs.getLong("refresh_token_expires_at"))
            .githubId(rs.getString("github_id"))
            .githubUsername(rs.getString("github_username"))
            .createdAt(rs.getLong("created_at"))
            .lastAccessedAt(rs.getLong("last_accessed_at"))
            .isActive(rs.getBoolean("is_active"))
            .build();

    @Override
    public UUID createSession(Session session) {
        var sql = "INSERT INTO sessions (id, user_id, access_token, refresh_token, " +
                "access_token_expires_at, refresh_token_expires_at, github_id, github_username, " +
                "created_at, last_accessed_at, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        UUID sessionId = session.getId() != null ? session.getId() : UUID.randomUUID();

        int result = jdbcTemplate.update(sql,
                DaoHelper.uuidToBytesArray(sessionId),
                DaoHelper.uuidToBytesArray(session.getUserId()),
                session.getAccessToken(),
                session.getRefreshToken(),
                session.getAccessTokenExpiresAt(),
                session.getRefreshTokenExpiresAt(),
                session.getGithubId(),
                session.getGithubUsername(),
                session.getCreatedAt(),
                session.getLastAccessedAt(),
                session.getIsActive() != null ? session.getIsActive() : true
        );

        return result == 1 ? sessionId : null;
    }

    @Override
    public Session findSessionByAccessToken(String accessToken) {
        var sql = "SELECT * FROM sessions WHERE access_token = ? AND is_active = true";
        List<Session> sessions = jdbcTemplate.query(sql, sessionRowMapper, accessToken);
        return sessions.isEmpty() ? null : sessions.getFirst();
    }

    @Override
    public Session findSessionByRefreshToken(String refreshToken) {
        var sql = "SELECT * FROM sessions WHERE refresh_token = ? AND is_active = true";
        List<Session> sessions = jdbcTemplate.query(sql, sessionRowMapper, refreshToken);
        return sessions.isEmpty() ? null : sessions.getFirst();
    }

    @Override
    public Session findActiveSessionByUserId(UUID userId) {
        var sql = "SELECT * FROM sessions WHERE user_id = ? AND is_active = true " +
                "ORDER BY created_at DESC LIMIT 1";
        List<Session> sessions = jdbcTemplate.query(sql, sessionRowMapper, (Object) DaoHelper.uuidToBytesArray((userId)));
        return sessions.isEmpty() ? null : sessions.getFirst();
    }

    @Override
    public boolean updateSessionAccessToken(UUID sessionId, String newAccessToken, Long expiresAt) {
        var sql = "UPDATE sessions SET access_token = ?, access_token_expires_at = ?, " +
                "last_accessed_at = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, newAccessToken, expiresAt,
                System.currentTimeMillis(), DaoHelper.uuidToBytesArray(sessionId));
        return result == 1;
    }

    @Override
    public boolean deactivateSession(UUID sessionId) {
        var sql = "UPDATE sessions SET is_active = false WHERE id = ?";
        int result = jdbcTemplate.update(sql, (Object) DaoHelper.uuidToBytesArray(sessionId));
        return result == 1;
    }

    @Override
    public boolean deactivateAllUserSessions(UUID userId) {
        var sql = "UPDATE sessions SET is_active = false WHERE user_id = ?";
        int result = jdbcTemplate.update(sql, (Object) DaoHelper.uuidToBytesArray(userId));
        return result >= 0;
    }

    @Override
    public void cleanupExpiredSessions() {
        var sql = "UPDATE sessions SET is_active = false " +
                "WHERE refresh_token_expires_at < ? AND is_active = true";
        jdbcTemplate.update(sql, System.currentTimeMillis());
    }
}
