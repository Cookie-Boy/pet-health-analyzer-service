package ru.sibsutis.pet_health_analyzer.core.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sibsutis.pet_health_analyzer.core.model.AnalysisResult;
import ru.sibsutis.pet_health_analyzer.core.model.VitalData;

import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class ModelAnalyzer {

    private final MinioService minioService;
    private final String modelObjectName;
    private final Random random = new Random();

    // Заглушка для модели (в реальности здесь может быть объект модели)
    private Object model;

    public ModelAnalyzer(MinioService minioService,
                         @Value("${minio.model-object}") String modelObjectName) {
        this.minioService = minioService;
        this.modelObjectName = modelObjectName;
    }

    @PostConstruct
    public void loadModel() {
        try {
            if (!minioService.objectExists(modelObjectName)) {
                System.err.println("Model file not found in MinIO, using stub analyzer");
                model = null;
                return;
            }

            try (InputStream is = minioService.downloadFile(modelObjectName)) {
                // Здесь должна быть десериализация модели (зависит от формата)
                // Для примера просто сохраняем факт загрузки
                model = "loaded";
                System.out.println("Model loaded from MinIO: " + modelObjectName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load model from MinIO", e);
        }
    }

    public AnalysisResult analyze(VitalData vitalData) {
        boolean isAnomaly = false;
        String anomalyType = null;
        Map<String, Object> details = new HashMap<>();

        // Пример пороговой логики (заглушка)
        if (vitalData.getHeartRate() > 180 || vitalData.getHeartRate() < 40) {
            isAnomaly = true;
            anomalyType = "ABNORMAL_HEART_RATE";
            details.put("heartRate", vitalData.getHeartRate());
        } else if (vitalData.getTemperature() > 39.5 || vitalData.getTemperature() < 37.0) {
            isAnomaly = true;
            anomalyType = "ABNORMAL_TEMPERATURE";
            details.put("temperature", vitalData.getTemperature());
        } else if (vitalData.getRespiration() > 40 || vitalData.getRespiration() < 10) {
            isAnomaly = true;
            anomalyType = "ABNORMAL_RESPIRATION";
            details.put("respiration", vitalData.getRespiration());
        } else if (vitalData.getDistanceFromHome() > 500) {
            isAnomaly = true;
            anomalyType = "TOO_FAR_FROM_HOME";
            details.put("distance", vitalData.getDistanceFromHome());
        }

        return AnalysisResult.builder()
                .petId(vitalData.getPetId())
                .isAnomaly(isAnomaly)
                .anomalyType(anomalyType)
                .details(details)
                .timestamp(Instant.now().getEpochSecond())
                .build();
    }
}