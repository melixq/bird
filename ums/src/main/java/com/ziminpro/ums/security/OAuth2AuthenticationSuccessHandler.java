package com.ziminpro.ums.security;

import com.ziminpro.ums.dtos.AuthResponse;
import com.ziminpro.ums.dtos.GitHubUserInfo;
import com.ziminpro.ums.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Component
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final AuthenticationService authenticationService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${frontend.success-redirect}")
    private String successRedirect;

    public OAuth2AuthenticationSuccessHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return Mono.defer(() -> {
            try {
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauth2Token.getPrincipal();

                // Extracting GitHub User Information
                var githubUser = new GitHubUserInfo();
                githubUser.setId(Objects.requireNonNull(oauth2User.getAttribute("id")).toString());
                githubUser.setLogin(oauth2User.getAttribute("login"));
                githubUser.setName(oauth2User.getAttribute("name"));

                // User can make their email private on GitHub, so it will be null
                String email = oauth2User.getAttribute("email");
                if (email == null || email.trim().isEmpty()) {
                    // Generate a default email using the GitHub login
                    // Format: github_username@github.local
                    email = oauth2User.getAttribute("login") + "@github.local";
                }
                githubUser.setEmail(email);

                // Authenticate and create session
                AuthResponse authResponse = authenticationService.authenticateGithubUser(githubUser);

                // Redirect to frontend with tokens
                String redirectUrl = UriComponentsBuilder
                        .fromUriString(frontendUrl + successRedirect)
                        .queryParam("access_token", authResponse.getAccessToken())
                        .queryParam("refresh_token", authResponse.getRefreshToken())
                        .queryParam("expires_in", authResponse.getExpiresIn())
                        .build()
                        .toUriString();

                var response = webFilterExchange.getExchange().getResponse();
                response.setStatusCode(HttpStatus.FOUND);
                response.getHeaders().setLocation(URI.create(redirectUrl));

                return response.setComplete();

            } catch (Exception e) {
                String errorRedirect = UriComponentsBuilder
                        .fromUriString(frontendUrl + "/auth/failure")
                        .queryParam("error", e.getMessage())
                        .build()
                        .toUriString();

                var response = webFilterExchange.getExchange().getResponse();
                response.setStatusCode(HttpStatus.FOUND);
                response.getHeaders().setLocation(URI.create(errorRedirect));

                return response.setComplete();
            }
        });
    }
}
