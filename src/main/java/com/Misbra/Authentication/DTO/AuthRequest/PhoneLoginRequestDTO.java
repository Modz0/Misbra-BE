package com.Misbra.Authentication.DTO.AuthRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class PhoneLoginRequestDTO {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid phone number format (E.164 required)")
    private String phone;

    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    private String password;
}
