package com.ziminpro.ums.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private UUID userId;
    private String email;
    private String name;
    private List<String> roles;
    private String jti; // JWT ID for blacklisting
    private Integer tokenVersion; // For invalidating all tokens
    private Long issuedAt;
    private Long expiresAt;
}
