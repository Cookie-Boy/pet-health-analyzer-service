package ru.sibsutis.pet_health_analyzer.api.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientRegistrationId = "health-analyzer";
    private OAuth2AuthorizedClient authorizedClient;

    @PostConstruct
    public void init() {
        refreshClientCredentials();
    }

    public String getFreshToken() {
        refreshIfNeeded();
        return authorizedClient.getAccessToken().getTokenValue();
    }

    private void refreshClientCredentials() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal("service-account")
                .build();

        authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null) {
            throw new IllegalStateException("Failed to obtain token");
        }
    }

    private void refreshIfNeeded() {
        if (authorizedClient == null ||
                authorizedClient.getAccessToken().getExpiresAt().isBefore(Instant.now().plusSeconds(60))) {
            refreshClientCredentials();
        }
    }
}
