package com.Misbra.Authentication.DTO.AuthResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private UserResponseDTO user;
}
