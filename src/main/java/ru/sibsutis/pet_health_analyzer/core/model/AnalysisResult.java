package ru.sibsutis.pet_health_analyzer.core.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AnalysisResult {
    private String petId;
    private boolean isAnomaly;
    private int anomalyClass;
    private AnomalyType anomalyType;
    private Map<String, Object> details;
    private long timestamp;
}