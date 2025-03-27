package com.Misbra.DTO;

import com.Misbra.Component.SessionQuestions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private String sessionId;
    private String userId;
    private String team1name;
    private String team2name;
    private int team1score;
    private int team2score;
    private List<String> gameCategories;

    @Builder.Default
    private Map<String, List<SessionQuestions>> categoryQuestionsMap = new HashMap<>();

//    // Method to get all questions as a flat list (derived property)
//    public List<SessionQuestions> getGameQuestions() {
//        return categoryQuestionsMap.values().stream()
//                .flatMap(List::stream)
//                .collect(Collectors.toList());
//    }
}