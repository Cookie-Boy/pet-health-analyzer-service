package ru.sibsutis.pet_health_analyzer.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import ru.sibsutis.pet_health_analyzer.core.model.AnalysisResult;
import ru.sibsutis.pet_health_analyzer.core.model.VitalData;

@Slf4j
@Service
public class PetAnalyzerService {

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;

    public PetAnalyzerService(MqttGateway mqttGateway, ObjectMapper objectMapper) {
        this.mqttGateway = mqttGateway;
        this.objectMapper = objectMapper;
    }

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleVitalData(Message<String> message) {
        String payload = message.getPayload();
        try {
            VitalData vitalData = objectMapper.readValue(payload, VitalData.class);

            // 2. Вызываем Python-модель (или встроенный анализатор) для определения аномалий
            AnalysisResult result = analyzeVitalData(vitalData);

            saveAnalysisResult(result);

            if (result.isAnomaly()) {
                String resultJson = objectMapper.writeValueAsString(result);
                mqttGateway.sendToMqtt(resultJson, "pet/analyzed");
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    private AnalysisResult analyzeVitalData(VitalData data) {
        // Здесь логика вызова модели или встроенного анализа
        // Возвращает объект AnalysisResult с petId, isAnomaly, anomalyType и т.д.
    }

    private void saveAnalysisResult(AnalysisResult result) {
        // Сохранение в PostgreSQL
    }
}