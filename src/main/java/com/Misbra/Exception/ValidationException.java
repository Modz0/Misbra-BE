package com.Misbra.Exception;

import com.Misbra.Exception.Validation.ValidationErrorDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final String code;
    private final List<ValidationErrorDTO> errors;

    public ValidationException(String code, List<ValidationErrorDTO> errors) {
        super("Validation error occurred");
        this.code = code;
        this.errors = errors;
    }
}