package com.Misbra.Authentication.DTO.AuthRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneRequestDTO {
    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phone;
}
