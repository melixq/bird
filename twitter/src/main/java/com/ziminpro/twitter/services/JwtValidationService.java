package com.ziminpro.twitter.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtValidationService {
    private final SecretKey secretKey;

    public JwtValidationService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Mono<Map<String, Object>> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                if (claims.getExpiration().before(new Date())) {
                    throw new RuntimeException("JWT Token expired!");
                }

                return Map.of(
                        "userId", claims.getSubject(),
                        "email", claims.get("email", String.class),
                        "name", claims.get("name", String.class),
                        "roles", claims.get("roles", List.class)
                );
            } catch (JwtException e) {
                throw new RuntimeException("Invalid JWT token: " + e.getMessage());
            }
        });
    }

    public Mono<String> getUserIdFromToken(String token) {
        return validateToken(token).map(claims -> (String) claims.get("userId"));
    }
}
