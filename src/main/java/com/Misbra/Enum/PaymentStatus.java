package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    IN_PROGRESS(1, "free question"),
    COMPLETED(2, "Payed question"),
    FAILED(3, "Payed question");


    private final int code;
    private final String description;


    public static PaymentStatus fromCode(int code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

