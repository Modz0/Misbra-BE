package com.Misbra.Authentication.DTO.AuthResponse;

import com.Misbra.Enum.RoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private RoleEnum role;
}
