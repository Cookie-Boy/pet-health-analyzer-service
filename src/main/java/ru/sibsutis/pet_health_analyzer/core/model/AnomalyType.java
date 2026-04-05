package ru.sibsutis.pet_health_analyzer.core.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AnomalyType {
    NORMAL(0, "NORMAL"),
    ABNORMAL_HEART_RATE(1, "ABNORMAL_HEART_RATE"),
    ABNORMAL_RESPIRATION(2, "ABNORMAL_RESPIRATION"),
    ABNORMAL_TEMPERATURE(3, "ABNORMAL_TEMPERATURE"),
    TOO_FAR_FROM_HOME(4, "TOO_FAR_FROM_HOME"),
    UNKNOWN(5, "UNKNOWN");

    private final int code;
    private final String description;

    AnomalyType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    public static AnomalyType fromCode(int code) {
        for (AnomalyType type : AnomalyType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return NORMAL;
    }

    public static AnomalyType fromDescription(String description) {
        for (AnomalyType type : AnomalyType.values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        return NORMAL;
    }
}