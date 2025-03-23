package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordStatus {
    ACTIVE(1, "Active"),
    DELETED(2, "Deleted");

    private final int code;
    private final String description;


    public static RecordStatus fromCode(int code) {
        for (RecordStatus status : RecordStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
