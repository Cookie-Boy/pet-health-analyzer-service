package ru.sibsutis.pet_health_analyzer.core.model;

import org.springframework.data.annotation.Id;
import lombok.Data;

@Deprecated
@Data
public class BreedNorm {
    @Id
    private String id;

    private String breed;
    private String testName;
    private String testCategory;
    private String parameter;
    private Double minValue;
    private Double maxValue;
    private String unit;
}