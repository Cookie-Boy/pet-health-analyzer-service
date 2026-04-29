package ru.sibsutis.pet_health_analyzer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sibsutis.pet_health_analyzer.core.model.PetResult;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResultDto {

    private String timestamp;
    private String petId;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Double temperature;
    private Integer activityLevel;
    private LocationDto location;
    private Boolean isAnomalous;
    private String anomalyReason;

    public static PetResultDto fromEntity(PetResult entity) {
        if (entity == null) {
            return null;
        }

        String timestampStr = null;
        if (entity.getTimestamp() != null) {
            timestampStr = Instant.ofEpochSecond(entity.getTimestamp())
                    .atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        Integer activityLevel = null;
        if (entity.getDetails() != null && entity.getDetails().containsKey("activityLevel")) {
            Object activity = entity.getDetails().get("activityLevel");
            if (activity instanceof Integer) {
                activityLevel = (Integer) activity;
            }
        }

        String anomalyReason = null;
        if (entity.isAnomalous() && entity.getAnomalyType() != null) {
            anomalyReason = String.format(
                    "Класс аномалии: %d, краткое описание: %s",
                    entity.getAnomalyClass(),
                    entity.getAnomalyType().getDescription()
            );
        }

        LocationDto location = null;
        if (entity.getLat() != null || entity.getLon() != null) {
            location = LocationDto.builder()
                    .lat(entity.getLat())
                    .lon(entity.getLon())
                    .build();
        }

        return PetResultDto.builder()
                .timestamp(timestampStr)
                .petId(entity.getPetId())
                .heartRate(entity.getHeartRate())
                .respiratoryRate(entity.getRespiration())
                .temperature(entity.getTemperature())
                .activityLevel(activityLevel)
                .location(location)
                .isAnomalous(entity.isAnomalous())
                .anomalyReason(anomalyReason)
                .build();
    }
}