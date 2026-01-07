package com.ziminpro.ums.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Mono<UUID> currentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (UUID) ctx.getAuthentication().getPrincipal());
    }

    public static Mono<List<String>> currentRoles() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getAuthorities()
                        .stream()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .toList());
    }

    public static Mono<Boolean> isAdmin() {
        return currentRoles().map(roles -> roles.contains("ADMIN"));
    }
}
