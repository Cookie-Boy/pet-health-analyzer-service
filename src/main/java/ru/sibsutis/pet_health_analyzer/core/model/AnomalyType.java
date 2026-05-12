package ru.sibsutis.pet_health_analyzer.core.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AnomalyType {
    NORMAL(0, "NORMAL", "Всё в порядке"),
    ABNORMAL_HEART_RATE(1, "ABNORMAL_HEART_RATE", "Проблемы с пульсом"),
    ABNORMAL_RESPIRATION(2, "ABNORMAL_RESPIRATION", "Проблемы с дыханием"),
    ABNORMAL_TEMPERATURE(3, "ABNORMAL_TEMPERATURE", "Проблемы с температурой"),
    TOO_FAR_FROM_HOME(4, "TOO_FAR_FROM_HOME", "Питомец слишком далеко от дома"),
    UNKNOWN(5, "UNKNOWN", "Не удалось определить состояние");

    private final int code;
    private final String description;
    private final String friendlyDescription;

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    public String getFriendlyDescription() {
        return friendlyDescription;
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

    public static AnomalyType fromFriendlyDescription(String friendlyDescription) {
        for (AnomalyType type : AnomalyType.values()) {
            if (type.friendlyDescription.equals(friendlyDescription)) {
                return type;
            }
        }
        return NORMAL;
    }
}