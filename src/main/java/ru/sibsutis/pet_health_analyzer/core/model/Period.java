package ru.sibsutis.pet_health_analyzer.core.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum Period {
    DAY,
    WEEK,
    MONTH;

    public Long calculateStartTimestamp() {
        Instant now = Instant.now();
        Instant start = switch (this) {
            case DAY -> now.minus(1, ChronoUnit.DAYS);
            case WEEK -> now.minus(7, ChronoUnit.DAYS);
            case MONTH -> now.minus(30, ChronoUnit.DAYS);
        };
        return start.getEpochSecond();
    }
}
