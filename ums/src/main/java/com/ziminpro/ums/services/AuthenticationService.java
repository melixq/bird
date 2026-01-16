package com.ziminpro.ums.services;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {
    private final UmsRepository umsRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public AuthenticationService(
            UmsRepository umsRepository,
            TokenBlacklistService tokenBlacklistService,
            JwtService jwtService
    ) {
        this.umsRepository = umsRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtService = jwtService;
    }

    public AuthResponse authenticateGithubUser(GitHubUserInfo gitHubUser) {
        User user = User.builder()
                .name(gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin())
                .email(gitHubUser.getEmail())
                .githubId(gitHubUser.getId())
                .avatarUrl(gitHubUser.getAvatarUrl())
                .tokenVersion(0)
                .build();

        UUID userId = umsRepository.createOrUpdateGithubUser(user);

        if (userId == null) {
            throw new RuntimeException("Failed to create or update User");
        }

        User completeUser = umsRepository.findUserByID(userId);

        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(completeUser, jti);
        String refreshToken = jwtService.generateRefreshToken(completeUser, jti);

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
            JwtClaims refreshTokenClaims = validateToken(refreshToken);

            if (jwtService.isTokenExpired(refreshToken)) {
                throw new RuntimeException("Refresh token expired");
            }

            String tokenType = jwtService.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new RuntimeException("Invalid token type");
            }

            var userId = jwtService.getUserIdFromToken(refreshToken);
            if (tokenBlacklistService.isTokenBlacklisted(refreshTokenClaims.getJti())) {
                throw new RuntimeException("Token has been revoked");
            }

            tokenBlacklistService.blacklistToken(
                    refreshTokenClaims.getJti(),
                    refreshTokenClaims.getExpiresAt()
            );

            User user = umsRepository.findUserByID(userId);
            if (user.getId() == null) {
                throw new RuntimeException("User not found");
            }

            String newJti = UUID.randomUUID().toString();
            String newAccessToken = jwtService.generateAccessToken(user, newJti);
            String newRefreshToken = jwtService.generateRefreshToken(user, newJti);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                    .tokenType("Bearer")
                    .user(user)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    public LogoutResponse logout(String accessToken) {
        try {
            JwtClaims claims = validateToken(accessToken);
            if (claims == null) {
                return LogoutResponse.builder()
                        .success(false)
                        .message("Invalid token")
                        .build();
            }

            boolean blacklisted = tokenBlacklistService.blacklistToken(
                    claims.getJti(),
                    claims.getExpiresAt()
            );

            if (blacklisted) {
                return LogoutResponse.builder()
                        .success(true)
                        .message("Successfully logged out")
                        .logoutTime(System.currentTimeMillis())
                        .tokenExpiresAt(claims.getExpiresAt())
                        .build();
            } else {
                return LogoutResponse.builder()
                        .success(false)
                        .message("Failed to blacklist token")
                        .build();
            }
        } catch (Exception e) {
            return LogoutResponse.builder()
                    .success(false)
                    .message("Logout failed: " + e.getMessage())
                    .build();
        }
    }

    public JwtClaims validateToken(String token) {
        boolean blacklisted = tokenBlacklistService.isTokenBlacklisted(token);
        if (blacklisted) {
            throw new RuntimeException("Token is blacklisted!");
        }
        return jwtService.validateAndParseToken(token);
    }
}
