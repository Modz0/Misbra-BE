package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentGatewayStatus {
    PAID(1, "PAID"),
    FAILED(2, "FAILED"),
    CAPTURED(3, "CAPTURED");



    private final int code;
    private final String description;


    public static PaymentGatewayStatus fromCode(int code) {
        for (PaymentGatewayStatus status : PaymentGatewayStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

