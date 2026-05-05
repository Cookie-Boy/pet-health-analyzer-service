package ru.sibsutis.pet_health_analyzer.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sibsutis.pet_health_analyzer.api.client.PythonServiceClient;
import ru.sibsutis.pet_health_analyzer.api.dto.PredictionDto;
import ru.sibsutis.pet_health_analyzer.core.model.PetResult;
import ru.sibsutis.pet_health_analyzer.core.model.AnomalyType;
import ru.sibsutis.pet_health_analyzer.core.model.PetVital;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelAnalyzer {

    private final PythonServiceClient pythonServiceClient;

    @Value("${thresholds.heart-rate-max}")
    private int maxHeartRate;

    @Value("${thresholds.heart-rate-min}")
    private int minHeartRate;

    @Value("${thresholds.respiration-max}")
    private int maxRespiration;

    @Value("${thresholds.respiration-min}")
    private int minRespiration;

    @Value("${thresholds.temperature-max}")
    private double maxTemperature;

    @Value("${thresholds.temperature-min}")
    private double minTemperature;

    @Value("${thresholds.distance-from-home-max}")
    private int maxDistanceFromHome;

    public PetResult analyze(PetVital petVital) {

        if (petVital.getHeartRate() < minHeartRate || petVital.getHeartRate() > maxHeartRate) {
            return buildAnomalyResult(petVital, 1, Map.of("heartRate", petVital.getHeartRate()));
        }

        if (petVital.getRespiration() < minRespiration || petVital.getRespiration() > maxRespiration) {
            return buildAnomalyResult(petVital, 2, Map.of("respiration", petVital.getRespiration()));
        }

        if (petVital.getTemperature() < minTemperature || petVital.getTemperature() > maxTemperature) {
            return buildAnomalyResult(petVital, 3, Map.of("temperature", petVital.getTemperature()));
        }

        if (petVital.getDistanceFromHome() > maxDistanceFromHome) {
            return buildAnomalyResult(petVital, 4, Map.of("distance", petVital.getDistanceFromHome()));
        }

        try {
            PredictionDto response = pythonServiceClient.predict(petVital);

            if (response == null) {
                throw new RuntimeException("Empty response from Python service");
            }

            Map<String, Object> details = new HashMap<>();
            details.put("probabilities", response.getProbabilities());

            return PetResult.builder()
                    .petId(petVital.getPetId())
                    .heartRate(petVital.getHeartRate())
                    .respiration(petVital.getRespiration())
                    .temperature(petVital.getTemperature())
                    .isAnomalous(response.getAnomalyClass() != 0)
                    .anomalyClass(response.getAnomalyClass())
                    .anomalyType(AnomalyType.fromCode(response.getAnomalyClass()))
                    .distanceFromHome(petVital.getDistanceFromHome())
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

        return PetResult.builder()
                .petId(petVital.getPetId())
                .heartRate(petVital.getHeartRate())
                .respiration(petVital.getRespiration())
                .temperature(petVital.getTemperature())
                .isAnomalous(true)
                .anomalyClass(anomalyClass)
                .anomalyType(AnomalyType.fromCode(anomalyClass))
                .distanceFromHome(petVital.getDistanceFromHome())
                .details(details)
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }

    private PetResult buildErrorResult(PetVital petVital, String errorMessage) {
        return PetResult.builder()
                .petId(petVital.getPetId())
                .heartRate(petVital.getHeartRate())
                .respiration(petVital.getRespiration())
                .temperature(petVital.getTemperature())
                .isAnomalous(false)
                .anomalyClass(0)
                .anomalyType(AnomalyType.NORMAL)
                .distanceFromHome(petVital.getDistanceFromHome())
                .details(Map.of("error", errorMessage))
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }
}