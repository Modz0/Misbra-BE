package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PromoCodeType {
    FREE_SESSION(1, "Free session"),
    DISCOUNT(2, "Discount code");

    private final int code;
    private final String description;


    public static PromoCodeType fromCode(int code) {
        for (PromoCodeType status : PromoCodeType.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
