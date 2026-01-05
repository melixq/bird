package com.ziminpro.ums.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private UUID id;
    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresAt;
    private Long refreshTokenExpiresAt;
    private String githubId;
    private String githubUsername;
    private Long createdAt;
    private Long lastAccessedAt;
    private Boolean isActive;
}