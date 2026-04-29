package ru.sibsutis.pet_health_analyzer.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sibsutis.pet_health_analyzer.core.model.HealthStats;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private String petId;
    private String generatedAt;
    private String period;
    private String summary;
    private List<String> recommendations;
    private HealthStats stats;
}