package ru.sibsutis.pet_health_analyzer.core.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.sibsutis.pet_health_analyzer.core.model.AnalysisResult;
import ru.sibsutis.pet_health_analyzer.core.model.AnomalyType;
import ru.sibsutis.pet_health_analyzer.core.model.VitalData;

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

    public AnalysisResult analyze(VitalData vitalData) {

        if (vitalData.getHeartRate() < minHeartRate || vitalData.getHeartRate() > maxHeartRate) {
            return buildAnomalyResult(vitalData, 1, Map.of("heartRate", vitalData.getHeartRate()));
        }

        if (vitalData.getHeartRate() < minRespiration || vitalData.getHeartRate() > maxRespiration) {
            return buildAnomalyResult(vitalData, 2, Map.of("respiration", vitalData.getRespiration()));
        }

        if (vitalData.getDistanceFromHome() > maxDistanceFromHome) {
            return buildAnomalyResult(vitalData, 4, Map.of("distance", vitalData.getDistanceFromHome()));
        }

        try {
            PredictionResponse response = webClient.post()
                    .uri("/predict")
                    .bodyValue(vitalData)
                    .retrieve()
                    .bodyToMono(PredictionResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("Empty response from Python service");
            }

            Map<String, Object> details = new HashMap<>();
            details.put("heartRate", vitalData.getHeartRate());
            details.put("respiration", vitalData.getRespiration());
            details.put("temperature", vitalData.getTemperature());
            details.put("probabilities", response.getProbabilities());

            return AnalysisResult.builder()
                    .petId(vitalData.getPetId())
                    .isAnomaly(response.getAnomalyClass() != 0)
                    .anomalyClass(response.getAnomalyClass())
                    .anomalyType(AnomalyType.fromCode(response.getAnomalyClass()))
                    .details(details)
                    .timestamp(Instant.now().getEpochSecond())
                    .build();

        } catch (Exception e) {
            log.error("Error calling Python service for petId: {}", vitalData.getPetId(), e);
            return buildErrorResult(vitalData, e.getMessage());
        }
    }

    private AnalysisResult buildAnomalyResult(VitalData vitalData, int anomalyClass, Map<String, Object> specificDetails) {
        Map<String, Object> details = new HashMap<>(specificDetails);
        details.put("heartRate", vitalData.getHeartRate());
        details.put("respiration", vitalData.getRespiration());
        details.put("temperature", vitalData.getTemperature());

        return AnalysisResult.builder()
                .petId(vitalData.getPetId())
                .isAnomaly(true)
                .anomalyClass(anomalyClass)
                .anomalyType(AnomalyType.fromCode(anomalyClass))
                .details(details)
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }

    private AnalysisResult buildErrorResult(VitalData vitalData, String errorMessage) {
        return AnalysisResult.builder()
                .petId(vitalData.getPetId())
                .isAnomaly(false)
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