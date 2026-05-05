package ru.sibsutis.pet_health_analyzer.api.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.sibsutis.pet_health_analyzer.api.dto.PredictionDto;
import ru.sibsutis.pet_health_analyzer.core.model.PetVital;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonServiceClient {
    private final RestClient restClient;
    private final TokenProvider tokenProvider;

    public PredictionDto predict(PetVital petVital) {
        String token = tokenProvider.getFreshToken();
        log.info("Fresh token for calling python-service: {}", token);
        return restClient.post()
                .uri("/api/predict")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .body(petVital)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}

