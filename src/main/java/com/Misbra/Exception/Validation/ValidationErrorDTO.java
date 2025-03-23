package com.Misbra.Exception.Validation;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationErrorDTO {
    private final String code;
    private final String[] params;
}