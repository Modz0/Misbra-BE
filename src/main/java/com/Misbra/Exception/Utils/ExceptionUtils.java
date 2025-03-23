package com.Misbra.Exception.Utils;

import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExceptionUtils {
    // For single errors (your preferred 3-line case)
    public void throwValidationError(String errorCode, String[] params) {
        List<ValidationErrorDTO> error = List.of(new ValidationErrorDTO(errorCode, params));
        throw new ValidationException("VALIDATION_FAILED", error);
    }

    // For multiple errors
    public void throwValidationException(List<ValidationErrorDTO> errors) {
        throw new ValidationException("VALIDATION_FAILED", errors);
    }

    // For custom error types
    public void throwValidationException(String exceptionCode, List<ValidationErrorDTO> errors) {
        throw new ValidationException(exceptionCode, errors);
    }
}