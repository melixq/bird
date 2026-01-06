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
    public Mono<ResponseEntity<Map<String, Object>>> login() {
        Map<String, Object> response = new HashMap<>();
        response.put(Constants.CODE, "200");
        response.put(Constants.MESSAGE, "Please redirect to /oauth2/authorization/github to start OAuth process");
        response.put(Constants.DATA, Map.of(
                "github_login_url", "/oauth2/authorization/github"
        ));
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .body(response));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(@RequestBody TokenRefreshRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            AuthResponse authResponse = authenticationService.refreshAccessToken(request.getRefreshToken());
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Token refreshed successfully");
            response.put(Constants.DATA, authResponse);
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        } catch (Exception e) {
            response.put(Constants.CODE, "401");
            response.put(Constants.MESSAGE, "Token refresh failed");
            response.put(Constants.DATA, e.getMessage());
            return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        }
    }

    @PostMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateToken(@RequestBody JwtValidationRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            JwtClaims claims = authenticationService.validateToken(request.getToken());
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Token is valid");
            response.put(Constants.DATA, claims);
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        } catch (Exception e) {
            response.put(Constants.CODE, "401");
            response.put(Constants.MESSAGE, "Invalid token");
            response.put(Constants.DATA, e.getMessage());
            return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        }
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, Object>>> logout(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authHeader.replace("Bearer ", "");
            LogoutResponse logoutResponse = authenticationService.logout(token);

            if (logoutResponse.isSuccess()) {
                response.put(Constants.CODE, "200");
                response.put(Constants.MESSAGE, "Logged out successfully");
                response.put(Constants.DATA, true);
            } else {
                response.put(Constants.CODE, "400");
                response.put(Constants.MESSAGE, "Logout failed");
                response.put(Constants.DATA, false);
            }
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        } catch (Exception e) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Logout error");
            response.put(Constants.DATA, e.getMessage());
            return Mono.just(ResponseEntity.status(500)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        }
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authHeader.replace("Bearer ", "");
            JwtClaims claims = authenticationService.validateToken(token);

            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "User info retrieved");
            response.put(Constants.DATA, claims);
            return Mono.just(ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        } catch (Exception e) {
            response.put(Constants.CODE, "401");
            response.put(Constants.MESSAGE, "Unauthorized");
            response.put(Constants.DATA, e.getMessage());
            return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response));
        }
    }
}
