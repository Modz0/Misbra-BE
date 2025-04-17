package com.Misbra.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionType {
    FREE(1, "free question"),
    PAYED(2, "Payed question"),;

    private final int code;
    private final String description;


    public static QuestionType fromCode(int code) {
        for (QuestionType status : QuestionType.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
