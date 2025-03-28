package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionStatus {
    IN_PROGRESS(1, "inProgress"),
    COMPLETED(2, "Completed");


    private final int code;
    private final String description;


    public static SessionStatus fromCode(int code) {
        for (SessionStatus status : SessionStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

