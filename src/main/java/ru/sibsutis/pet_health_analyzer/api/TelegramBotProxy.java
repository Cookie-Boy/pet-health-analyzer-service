package ru.sibsutis.pet_health_analyzer.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class TelegramBotProxy {

    private final OAuth2AuthorizedClientManager clientManager;
    private final RestClient restClient;

    private final String clientRegistrationId = "pet_health_analyzer";

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Autowired
    public TelegramBotProxy(OAuth2AuthorizedClientManager clientManager,
                            RestClient.Builder builder) {
        this.clientManager = clientManager;
        this.restClient = builder.build();
    }

    private String getFreshToken() {
        OAuth2AuthorizedClient client = clientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
                        .principal("service-account")
                        .build()
        );

        return client != null ? client.getAccessToken().getTokenValue() : "token-is-null";
    }

    public void sendNotification(String tgUserName, String text) {
        String token = getFreshToken();
        log.info("Fresh token: {}", token);
        restClient.post()
                .uri(gatewayUrl + "/notify/{tgUserName}", tgUserName)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .body(text)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
