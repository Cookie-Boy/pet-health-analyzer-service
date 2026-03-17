package ru.sibsutis.pet_health_analyzer.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String petId;
    private boolean isAnomaly;
    private int anomalyClass;
    private String anomalyType;
    private Map<String, Object> details;
    private Long timestamp;
}
