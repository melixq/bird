package com.ziminpro.ums.controllers;

import com.ziminpro.ums.dtos.*;
import com.ziminpro.ums.services.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public Mono<ResponseEntity<ApiResponse<?>>> login() {
        Map<String, String> data = Map.of(
                "github_login_url", "/oauth2/authorization/github"
        );

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .body(ApiResponse.success(data)));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse<?>>> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            AuthResponse authResponse = authenticationService.refreshAccessToken(request.getRefreshToken());
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.success(authResponse)));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.error(401, "Token refresh failed", e.getMessage())));
        }
    }

    @PostMapping("/validate")
    public Mono<ResponseEntity<ApiResponse<?>>> validateToken(@RequestBody JwtValidationRequest request) {
        try {
            JwtClaims claims = authenticationService.validateToken(request.getToken());
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.success(claims)));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.error(401, "Invalid token", e.getMessage())));
        }
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<?>>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            LogoutResponse logoutResponse = authenticationService.logout(token);

            if (logoutResponse.isSuccess()) {
                return Mono.just(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                        .body(ApiResponse.success(true)));
            } else {
                return Mono.just(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                        .body(ApiResponse.error(400, "Logout failed", false)));
            }
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(500)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.error(500, "Logout error", e.getMessage())));
        }
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<?>>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            JwtClaims claims = authenticationService.validateToken(token);
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.success(claims)));
        } catch (Exception e) {
            return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(ApiResponse.error(401, "Unauthorized", e.getMessage())));
        }
    }
}
