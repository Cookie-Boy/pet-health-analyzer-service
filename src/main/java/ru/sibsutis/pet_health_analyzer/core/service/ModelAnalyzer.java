package ru.sibsutis.pet_health_analyzer.core.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.sibsutis.pet_health_analyzer.core.model.AnalysisResult;
import ru.sibsutis.pet_health_analyzer.core.model.VitalData;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ModelAnalyzer {

    private final WebClient webClient;

    @Value("${thresholds.distance-from-home-max}")
    private int maxDistanceFromHome;

    public ModelAnalyzer(@Value("${python.url}") String pythonServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(pythonServiceUrl)
                .build();
    }

    public AnalysisResult analyze(VitalData vitalData) {
        if (vitalData.getDistanceFromHome() > maxDistanceFromHome) {
            return AnalysisResult.builder()
                    .petId(vitalData.getPetId())
                    .isAnomaly(true)
                    .anomalyClass(4)
                    .anomalyType("TOO_FAR_FROM_HOME")
                    .details(Map.of("distance", vitalData.getDistanceFromHome()))
                    .timestamp(Instant.now().getEpochSecond())
                    .build();
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
            details.put("probabilities", response.getProbabilities()); // Может быть null

            return AnalysisResult.builder()
                    .petId(vitalData.getPetId())
                    .isAnomaly(response.getAnomalyClass() != 0)
                    .anomalyClass(response.getAnomalyClass())
                    .anomalyType(mapClassToType(response.getAnomalyClass()))
                    .details(details)
                    .timestamp(Instant.now().getEpochSecond())
                    .build();

        } catch (Exception e) {
            log.error("Error calling Python service for petId: {}", vitalData.getPetId(), e);
            // В случае ошибки возвращаем "норма", чтобы не спамить уведомлениями
            return AnalysisResult.builder()
                    .petId(vitalData.getPetId())
                    .isAnomaly(false)
                    .anomalyClass(0)
                    .anomalyType(null)
                    .details(Map.of("error", e.getMessage()))
                    .timestamp(Instant.now().getEpochSecond())
                    .build();
        }
    }

    private String mapClassToType(int anomalyClass) {
        return switch (anomalyClass) {
            case 1 -> "ABNORMAL_HEART_RATE";
            case 2 -> "ABNORMAL_RESPIRATION";
            case 3 -> "ABNORMAL_TEMPERATURE";
            default -> null;
        };
    }

    @Setter
    @Getter
    private static class PredictionResponse {
        private int anomalyClass;
        private List<Double> probabilities;
    }
}