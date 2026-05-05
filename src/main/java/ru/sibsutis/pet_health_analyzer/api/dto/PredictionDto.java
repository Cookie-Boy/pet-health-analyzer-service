package ru.sibsutis.pet_health_analyzer.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PredictionDto {
    private int anomalyClass;
    private List<Double> probabilities;
}