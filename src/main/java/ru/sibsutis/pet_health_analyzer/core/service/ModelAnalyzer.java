package ru.sibsutis.pet_health_analyzer.core.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.sibsutis.pet_health_analyzer.core.model.PetResult;
import ru.sibsutis.pet_health_analyzer.core.model.AnomalyType;
import ru.sibsutis.pet_health_analyzer.core.model.PetVital;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ModelAnalyzer {

    private final WebClient webClient;

    @Value("${thresholds.heart-rate-max}")
    private int maxHeartRate;

    @Value("${thresholds.heart-rate-min}")
    private int minHeartRate;

    @Value("${thresholds.respiration-max}")
    private int maxRespiration;

    @Value("${thresholds.respiration-min}")
    private int minRespiration;

    @Value("${thresholds.distance-from-home-max}")
    private int maxDistanceFromHome;

    public ModelAnalyzer(@Value("${python.url}") String pythonServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(pythonServiceUrl)
                .build();
    }

    public PetResult analyze(PetVital petVital) {

        if (petVital.getHeartRate() < minHeartRate || petVital.getHeartRate() > maxHeartRate) {
            return buildAnomalyResult(petVital, 1, Map.of("heartRate", petVital.getHeartRate()));
        }

        if (petVital.getHeartRate() < minRespiration || petVital.getHeartRate() > maxRespiration) {
            return buildAnomalyResult(petVital, 2, Map.of("respiration", petVital.getRespiration()));
        }

        if (petVital.getDistanceFromHome() > maxDistanceFromHome) {
            return buildAnomalyResult(petVital, 4, Map.of("distance", petVital.getDistanceFromHome()));
        }

        try {
            PredictionResponse response = webClient.post()
                    .uri("/predict")
                    .bodyValue(petVital)
                    .retrieve()
                    .bodyToMono(PredictionResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Python service");
            }

            Map<String, Object> details = new HashMap<>();
            details.put("heartRate", petVital.getHeartRate());
            details.put("respiration", petVital.getRespiration());
            details.put("temperature", petVital.getTemperature());
            details.put("probabilities", response.getProbabilities());

            return PetResult.builder()
                    .petId(petVital.getPetId())
                    .isAnomalous(response.getAnomalyClass() != 0)
                    .anomalyClass(response.getAnomalyClass())
                    .anomalyType(AnomalyType.fromCode(response.getAnomalyClass()))
                    .details(details)
                    .timestamp(Instant.now().getEpochSecond())
                    .build();

        } catch (Exception e) {
            log.error("Error calling Python service for petId: {}", petVital.getPetId(), e);
            return buildErrorResult(petVital, e.getMessage());
        }
    }

    private PetResult buildAnomalyResult(PetVital petVital, int anomalyClass, Map<String, Object> specificDetails) {
        Map<String, Object> details = new HashMap<>(specificDetails);
        details.put("heartRate", petVital.getHeartRate());
        details.put("respiration", petVital.getRespiration());
        details.put("temperature", petVital.getTemperature());

        return PetResult.builder()
                .petId(petVital.getPetId())
                .isAnomalous(true)
                .anomalyClass(anomalyClass)
                .anomalyType(AnomalyType.fromCode(anomalyClass))
                .details(details)
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }

    private PetResult buildErrorResult(PetVital petVital, String errorMessage) {
        return PetResult.builder()
                .petId(petVital.getPetId())
                .isAnomalous(false)
                .anomalyClass(0)
                .anomalyType(AnomalyType.NORMAL)
                .details(Map.of("error", errorMessage))
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }

    @Setter
    @Getter
    private static class PredictionResponse {
        private int anomalyClass;
        private List<Double> probabilities;
    }
}