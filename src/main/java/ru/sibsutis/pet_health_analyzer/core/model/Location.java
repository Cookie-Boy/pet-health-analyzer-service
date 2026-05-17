package ru.sibsutis.pet_health_analyzer.core.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private Double lat;
    private Double lon;
    private Double distanceFromHome;
}
