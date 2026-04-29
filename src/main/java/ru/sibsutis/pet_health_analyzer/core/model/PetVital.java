package ru.sibsutis.pet_health_analyzer.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetVital {
    private String petId;
    private String species;
    private String breed;

    private Integer heartRate;
    private Integer respiration;
    private Double temperature;

    private Double distanceFromHome;

    private Long timestamp;

    @JsonProperty("timestamp")
    public Long getTimestamp() {
        return timestamp != null ? timestamp : Instant.now().getEpochSecond();
    }
}