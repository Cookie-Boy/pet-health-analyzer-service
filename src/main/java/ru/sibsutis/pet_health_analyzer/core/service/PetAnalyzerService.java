package ru.sibsutis.pet_health_analyzer.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final ModelAnalyzer modelAnalyzer;

    @Value("${mqtt.outbound.topic}")
    private String outboundTopic;

    @Autowired
    public PetAnalyzerService(MqttGateway mqttGateway,
                              ModelAnalyzer modelAnalyzer) {
        this.mqttGateway = mqttGateway;
        this.objectMapper = new ObjectMapper();
        this.modelAnalyzer = modelAnalyzer;
    }

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleVitalData(Message<String> message) {
        String payload = message.getPayload();
        try {
            VitalData vitalData = objectMapper.readValue(payload, VitalData.class);

            AnalysisResult result = modelAnalyzer.analyze(vitalData);

            saveAnalysisResult(result);

            if (result.isAnomaly()) {
                String resultJson = objectMapper.writeValueAsString(result);
                mqttGateway.sendToMqtt(resultJson, outboundTopic);
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    private void saveAnalysisResult(AnalysisResult result) {
        // Сохранение в PostgreSQL
    }
}