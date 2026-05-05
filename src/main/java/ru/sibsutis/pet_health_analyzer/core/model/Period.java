package ru.sibsutis.pet_health_analyzer.core.model;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
public enum Period {
    DAY,
    WEEK,
    MONTH;

    public static Period fromString(String value) {
        if (value == null || value.isEmpty()) {
            return WEEK;
        }

        try {
            return Period.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Unable to convert value '{}' to PeriodEnum", value);
            return WEEK;
        }
    }

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
