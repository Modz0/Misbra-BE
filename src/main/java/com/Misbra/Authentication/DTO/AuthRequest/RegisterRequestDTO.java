package com.Misbra.Authentication.DTO.AuthRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164 required)")
    private String phone;

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotBlank
    private String firstName; // First Name

    @NotBlank
    private String email; // First Name

    @NotBlank
    private String lastName; // Last Name

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;
}
