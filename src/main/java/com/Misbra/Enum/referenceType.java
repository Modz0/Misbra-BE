package com.Misbra.Enum;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum referenceType {
    QUESTION(1, "Question"),
    CATEGORY(2, "Category"),
    ANSWER(3,"Answer");


    private final int code;
    private final String description;

    public static referenceType fromCode(int code) {
        for (referenceType type : referenceType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown place type code: " + code);
    }
}
