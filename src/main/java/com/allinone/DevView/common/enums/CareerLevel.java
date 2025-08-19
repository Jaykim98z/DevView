package com.allinone.DevView.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CareerLevel {
    INTERN("INTERN"),
    JUNIOR("JUNIOR"),
    MID("MID"),
    SENIOR("SENIOR"),
    LEAD("LEAD"),
    MANAGER("MANAGER"),;

    private final String displayName;

    public static CareerLevel fromString(String value) {
        if (value == null) return null;
        try {
            return CareerLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
