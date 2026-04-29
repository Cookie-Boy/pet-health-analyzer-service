package ru.sibsutis.pet_health_analyzer.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStats {
    private Double avgHeartRate;
    private Double avgRespiratoryRate;
    private Double avgTemperature;
    private Integer anomalyCount;
    private Integer totalReadings;
}