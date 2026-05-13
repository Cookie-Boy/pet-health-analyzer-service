package ru.sibsutis.pet_health_analyzer.core.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Location {
    private Double lat;
    private Double lon;
    private Double distanceFromHome;
}
