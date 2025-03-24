package com.Misbra.DTO;

import com.Misbra.Component.SessionQuestions;
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
public class SessionDTO {
    private String sessionId;
    private String userId;
    private String team1Id;
    private String team2Id;
    private int team1score;
    private int team2score;

    @Builder.Default
    private List<String> gameCategories = new ArrayList<>();

    @Builder.Default
    private List<SessionQuestions> gameQuestions = new ArrayList<>();
}