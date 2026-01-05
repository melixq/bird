package com.ziminpro.ums.services;

import com.ziminpro.ums.dao.SessionRepository;
import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {
    private final UmsRepository umsRepository;
    private final SessionRepository sessionRepository;
    private final JwtService jwtService;

    public AuthenticationService(UmsRepository umsRepository, SessionRepository sessionRepository, JwtService jwtService) {
        this.umsRepository = umsRepository;
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticateGithubUser(GitHubUserInfo gitHubUser) {
        User user = User.builder()
                .name(gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin())
                .email(gitHubUser.getEmail())
                .githubId(gitHubUser.getId())
                .build();

        UUID userId = umsRepository.createOrUpdateGithubUser(user);

        if (userId == null) {
            throw new RuntimeException("Failed to create or update User");
        }

        User completeUser = umsRepository.findUserByID(userId);
        sessionRepository.deactivateAllUserSessions(userId);

        String accessToken = jwtService.generateAccessToken(completeUser);
        String refreshToken = jwtService.generateRefreshToken(completeUser);

        long now = System.currentTimeMillis();
        Session session = Session.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(now + jwtService.getAccessTokenExpiration())
                .refreshTokenExpiresAt(now + jwtService.getRefreshTokenExpiration())
                .githubId(gitHubUser.getId())
                .githubUsername(gitHubUser.getLogin())
                .createdAt(now)
                .lastAccessedAt(now)
                .isActive(true)
                .build();

        sessionRepository.createSession(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .user(completeUser)
                .build();
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        try {
            if (jwtService.isTokenExpired(refreshToken)) {
                throw new RuntimeException("Refresh token expired");
            }

            String tokenType = jwtService.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new RuntimeException("Invalid token type");
            }

            Session session = sessionRepository.findSessionByRefreshToken(refreshToken);
            if (session == null || !session.getIsActive()) {
                throw new RuntimeException("Session not found or inactive");
            }

            User user = umsRepository.findUserByID(session.getUserId());
            if (user.getId() == null) {
                throw new RuntimeException("User not found");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            long expiresAt = System.currentTimeMillis() + jwtService.getAccessTokenExpiration();

            sessionRepository.updateSessionAccessToken(session.getId(), newAccessToken, expiresAt);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                    .tokenType("Bearer")
                    .user(user)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    public boolean logout(String accessToken) {
        Session session = sessionRepository.findSessionByAccessToken(accessToken);
        if (session != null) {
            return sessionRepository.deactivateSession(session.getId());
        }
        return false;
    }

    public JwtClaims validateToken(String token) {
        return jwtService.validateAndParseToken(token);
    }
}
