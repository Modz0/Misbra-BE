package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionType {
    FREE(1, "free session"),
    PAYED(2, "Payed session"),;

    private final int code;
    private final String description;


    public static SessionType fromCode(int code) {
        for (SessionType status : SessionType.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

