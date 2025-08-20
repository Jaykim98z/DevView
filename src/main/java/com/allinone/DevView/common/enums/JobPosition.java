package com.allinone.DevView.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobPosition {
    BACKEND("BACKEND"),
    FRONTEND("FRONTEND"),
    FULLSTACK("FULLSTACK"),
    DEVOPS("DEVOPS"),
    DATA_AI("DATA/AI");

    private final String displayName;

    public static JobPosition fromString(String value) {
        if (value == null) return null;
        try {
            return JobPosition.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
