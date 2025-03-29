package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PowerupType {
    MINUS_FIFTY_PERCENT(1, "minus fifty percent"),
    NO_ANSWER(2, "no answer"),
    DOUBLE_OR_MINUS
            (3, "double or minus");


    private final int code;
    private final String description;


    public static PowerupType
    fromCode(int code) {for (PowerupType status : PowerupType.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

