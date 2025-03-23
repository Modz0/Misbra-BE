package com.Misbra.Exception;

import com.Misbra.Component.BilingualError;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private List<BilingualError> errors;
}
