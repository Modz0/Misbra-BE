package com.Misbra.DTO;

import com.Misbra.Enum.RecordStatus;
import com.Misbra.Enum.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String phone;
    private RecordStatus recordStatus;
    private RoleEnum role;
    private boolean enabled;
    private Long numberOfGamesPlayed;
    private Long numberOfGamesRemaining;
    private Long numberOfFreeGames;
    // Only track answered questions
    @Builder.Default
    private List<String> answeredQuestionIds = new ArrayList<>();
}