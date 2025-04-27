package com.Misbra.Exception;

import com.Misbra.Exception.Validation.ValidationErrorDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentException extends RuntimeException {
    private final List<ValidationErrorDTO> errors;

    public PaymentException(List<ValidationErrorDTO> errors) {
        super("Payment processing error occurred");

        this.errors = errors;
    }
}

