package com.ziminpro.ums.security;

import com.ziminpro.ums.services.JwtService;
import com.ziminpro.ums.services.TokenBlacklistService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();

        // OAuth and public endpoints MUST bypass JWT
        if (path.startsWith("/oauth2")
                || path.startsWith("/login/oauth2")
                || path.startsWith("/auth")
                || path.equals("/roles")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        return Mono.fromCallable(() -> jwtService.validateAndParseToken(token))
                .flatMap(claims -> {

                    if (tokenBlacklistService.isTokenBlacklisted(claims.getJti())) {
                        return unauthorized(exchange);
                    }

                    List<SimpleGrantedAuthority> authorities =
                            claims.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    claims.getUserId(),
                                    null,
                                    authorities
                            );

                    authentication.setDetails(claims);

                    return chain.filter(exchange)
                            .contextWrite(
                                    ReactiveSecurityContextHolder.withAuthentication(authentication)
                            );
                })
                .onErrorResume(e -> unauthorized(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
