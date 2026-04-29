package ru.sibsutis.pet_health_analyzer.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import ru.sibsutis.pet_health_analyzer.api.dto.LatestPetResultDto;
import ru.sibsutis.pet_health_analyzer.api.dto.PetResultDto;
import ru.sibsutis.pet_health_analyzer.api.dto.RecommendationDto;
import ru.sibsutis.pet_health_analyzer.core.model.*;
import ru.sibsutis.pet_health_analyzer.core.repository.PetResultRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetAnalyzerService {

    private static final int COLLAR_ONLINE_MINUTES = 5;

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ModelAnalyzer modelAnalyzer;
    private final PetResultRepository petResultRepository;

    @Value("${mqtt.outbound.topic}")
    private String outboundTopic;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleVitalData(Message<String> message) {
        String payload = message.getPayload();
        try {
            PetVital petVital = objectMapper.readValue(payload, PetVital.class);
            PetResult result = modelAnalyzer.analyze(petVital);
            petResultRepository.save(result);

            if (result.isAnomalous()) {
                String resultJson = objectMapper.writeValueAsString(result);
                mqttGateway.sendToMqtt(resultJson, outboundTopic);
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    public List<PetResultDto> getVitalsHistory(String petId, Period period) {
        Long startTimestamp = period.calculateStartTimestamp();

        List<PetResult> petResults = petResultRepository
                .findByPetIdAndTimestampGreaterThanEqualOrderByTimestampAsc(petId, startTimestamp);

        return petResults.stream()
                .map(PetResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    public LatestPetResultDto getLatestVitals(String petId) {
        log.info("Getting latest vitals for pet: {}", petId);

        Optional<PetResult> latestResult = petResultRepository
                .findTopByPetIdOrderByTimestampDesc(petId);

        if (latestResult.isEmpty()) {
            log.warn("No vitals found for pet: {}", petId);
            return null;
        }

        PetResult petResult = latestResult.get();
        PetResultDto vitalsDto = PetResultDto.fromEntity(petResult);

        String collarStatus = determineCollarStatus(petResult.getTimestamp());

        return LatestPetResultDto.builder()
                .timestamp(vitalsDto.getTimestamp())
                .petId(vitalsDto.getPetId())
                .heartRate(vitalsDto.getHeartRate())
                .respiratoryRate(vitalsDto.getRespiratoryRate())
                .temperature(vitalsDto.getTemperature())
                .activityLevel(vitalsDto.getActivityLevel())
                .location(vitalsDto.getLocation())
                .isAnomalous(vitalsDto.getIsAnomalous())
                .anomalyReason(vitalsDto.getAnomalyReason())
                .collarStatus(collarStatus)
                .build();
    }

    public RecommendationDto getRecommendations(String petId, Period period) {
        log.info("Getting recommendations for pet: {}, period: {}", petId, period);

        Long startTimestamp = period.calculateStartTimestamp();
        List<PetResult> vitalsHistory = petResultRepository
                .findByPetIdAndTimestampGreaterThanEqualOrderByTimestampAsc(petId, startTimestamp);

        if (vitalsHistory.isEmpty()) {
            log.warn("No vitals data found for pet: {} in period: {}", petId, period);
            return generateEmptyRecommendation(petId, period);
        }

        HealthStats stats = calculateStats(vitalsHistory);

        String summary = generateSummary(vitalsHistory, stats, period);
        List<String> recommendations = generateRecommendations(vitalsHistory, stats);

        return RecommendationDto.builder()
                .petId(petId)
                .generatedAt(Instant.now().toString())
                .period(period.name().toLowerCase())
                .summary(summary)
                .recommendations(recommendations)
                .stats(stats)
                .build();
    }

    private RecommendationDto generateEmptyRecommendation(String petId, Period period) {
        return RecommendationDto.builder()
                .petId(petId)
                .generatedAt(Instant.now().toString())
                .period(period.name().toLowerCase())
                .summary("Недостаточно данных для анализа за указанный период")
                .recommendations(List.of(
                        "Пожалуйста, убедитесь, что ошейник питомца работает корректно",
                        "Пополните данные мониторинга в ближайшее время",
                        "При отсутствии данных в течение длительного времени обратитесь к ветеринару"
                ))
                .stats(HealthStats.builder()
                        .avgHeartRate(0.0)
                        .avgRespiratoryRate(0.0)
                        .avgTemperature(0.0)
                        .anomalyCount(0)
                        .totalReadings(0)
                        .build())
                .build();
    }

    public RecommendationDto analyzeAndGenerateRecommendations(String petId, Period period) {
        log.info("Analyzing pet: {} for period: {}", petId, period);

        Long startTimestamp = period.calculateStartTimestamp();
        List<PetResult> vitalsHistory = petResultRepository
                .findByPetIdAndTimestampGreaterThanEqualOrderByTimestampAsc(petId, startTimestamp);

        if (vitalsHistory.isEmpty()) {
            log.warn("No vitals data found for pet: {} in period: {}", petId, period);
            throw new RuntimeException("No vitals data available for analysis");
        }

        HealthStats stats = calculateStats(vitalsHistory);

        String summary = generateSummary(vitalsHistory, stats, period);
        List<String> recommendations = generateRecommendations(vitalsHistory, stats);

        return RecommendationDto.builder()
                .petId(petId)
                .generatedAt(Instant.now().toString())
                .period(period.name().toLowerCase())
                .summary(summary)
                .recommendations(recommendations)
                .stats(stats)
                .build();
    }

    private String determineCollarStatus(Long lastTimestamp) {
        if (lastTimestamp == null) {
            return "offline";
        }

        Instant lastUpdate = Instant.ofEpochSecond(lastTimestamp);
        Instant now = Instant.now();
        long minutesDiff = Duration.between(lastUpdate, now).toMinutes();

        return minutesDiff < COLLAR_ONLINE_MINUTES ? "online" : "offline";
    }

    private HealthStats calculateStats(List<PetResult> vitalsHistory) {
        if (vitalsHistory.isEmpty()) {
            return HealthStats.builder()
                    .avgHeartRate(0.0)
                    .avgRespiratoryRate(0.0)
                    .avgTemperature(0.0)
                    .anomalyCount(0)
                    .totalReadings(0)
                    .build();
        }

        double avgHeartRate = vitalsHistory.stream()
                .mapToInt(PetResult::getHeartRate)
                .average()
                .orElse(0.0);

        double avgRespiratoryRate = vitalsHistory.stream()
                .mapToInt(PetResult::getRespiration)
                .average()
                .orElse(0.0);

        double avgTemperature = vitalsHistory.stream()
                .mapToDouble(PetResult::getTemperature)
                .average()
                .orElse(0.0);

        long anomalyCount = vitalsHistory.stream()
                .filter(PetResult::isAnomalous)
                .count();

        return HealthStats.builder()
                .avgHeartRate(Math.round(avgHeartRate * 10) / 10.0)
                .avgRespiratoryRate(Math.round(avgRespiratoryRate * 10) / 10.0)
                .avgTemperature(Math.round(avgTemperature * 10) / 10.0)
                .anomalyCount((int) anomalyCount)
                .totalReadings(vitalsHistory.size())
                .build();
    }

    private String generateSummary(List<PetResult> vitalsHistory, HealthStats stats, Period period) {
        String periodText = switch (period) {
            case DAY -> "день";
            case WEEK -> "неделю";
            case MONTH -> "месяц";
        };

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("За прошедшую %s обнаружено %d аномальных показателей. ",
                periodText, stats.getAnomalyCount()));

        if (stats.getAnomalyCount() > 0) {
            Map<AnomalyType, Long> anomalyTypes = vitalsHistory.stream()
                    .filter(PetResult::isAnomalous)
                    .collect(Collectors.groupingBy(PetResult::getAnomalyType, Collectors.counting()));

            if (!anomalyTypes.isEmpty()) {
                summary.append("Основные проблемы: ");
                anomalyTypes.entrySet().stream()
                        .limit(3)
                        .forEach(entry -> summary.append(entry.getKey().getDescription())
                                .append(" (").append(entry.getValue()).append(" раз), "));
                summary.setLength(summary.length() - 2);
                summary.append(". ");
            }
        }

        summary.append(String.format("Средние значения в норме: пульс %.1f, дыхание %.1f, температура %.1f.",
                stats.getAvgHeartRate(), stats.getAvgRespiratoryRate(), stats.getAvgTemperature()));

        return summary.toString();
    }

    private List<String> generateRecommendations(List<PetResult> vitalsHistory, HealthStats stats) {
        List<String> recommendations = new ArrayList<>();

        if (stats.getAnomalyCount() > 0) {
            recommendations.add("Рекомендуется проконсультироваться с ветеринаром");

            Map<AnomalyType, Long> anomalyTypes = vitalsHistory.stream()
                    .filter(PetResult::isAnomalous)
                    .collect(Collectors.groupingBy(PetResult::getAnomalyType, Collectors.counting()));

            if (anomalyTypes.containsKey(AnomalyType.ABNORMAL_HEART_RATE)) {
                recommendations.add("Выявлены проблемы с сердечным ритмом. Рекомендуется снизить стрессовые нагрузки");
            }

            if (anomalyTypes.containsKey(AnomalyType.ABNORMAL_RESPIRATION)) {
                recommendations.add("Обнаружены нарушения дыхания. Проверьте качество воздуха в помещении");
            }

            if (anomalyTypes.containsKey(AnomalyType.ABNORMAL_TEMPERATURE)) {
                recommendations.add("Зафиксированы отклонения температуры. Обеспечьте комфортный температурный режим");
            }

            if (anomalyTypes.containsKey(AnomalyType.TOO_FAR_FROM_HOME)) {
                recommendations.add("Питомец слишком далеко от дома! Будьте внимательны.");
            }
        }

        if (stats.getAvgHeartRate() > 100) {
            recommendations.add("Учащенный пульс в среднем по периоду. Рекомендуется консультация кардиолога");
        } else if (stats.getAvgHeartRate() < 60) {
            recommendations.add("Замедленный пульс в среднем по периоду. Требуется наблюдение специалиста");
        }

        if (stats.getAvgTemperature() > 39.0) {
            recommendations.add("Повышенная температура в среднем по периоду. Проверьте на наличие воспалительных процессов");
        } else if (stats.getAvgTemperature() < 37.5) {
            recommendations.add("Пониженная температура в среднем по периоду. Обеспечьте дополнительное тепло");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Показатели в норме. Продолжайте регулярное наблюдение за питомцем");
            recommendations.add("Поддерживайте текущий режим кормления и прогулок");
        }

        return recommendations;
    }
}