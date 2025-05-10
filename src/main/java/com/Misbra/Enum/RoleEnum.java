package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ADMIN(1, "ADMIN"),
    USER(2, "USER"),
    MAINTAINER(3, "MAINTAINER");

    private final int code;
    private final String description;


    public static RoleEnum fromCode(int code) {
        for (RoleEnum status : RoleEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
