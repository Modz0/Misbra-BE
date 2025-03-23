package com.Misbra.DTO;

import com.Misbra.Enum.RecordStatus;
import com.Misbra.Enum.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> visitedPlaces;
    private RecordStatus recordStatus;
    private String phone;
    private String password;
    private RoleEnum role;
    private boolean enabled ;
}
