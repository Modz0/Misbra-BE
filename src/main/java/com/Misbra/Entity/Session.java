package com.Misbra.Entity;

import com.Misbra.Component.SessionQuestions;
import com.Misbra.Enum.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sessions")
public class Session {
    @Id
    private String sessionId;
    private String userId;
    private String team1name;
    private String team2name;
    private int team1score;
    private int team2score;
    private List<String> gameCategories;
    private int totalQuestions;
    private int answeredQuestions;
    private SessionStatus sessionStatus;
    private Map<String, List<SessionQuestions>> categoryQuestionsMap = new HashMap<>();

//    // Helper method to get all questions as a flat list
//    public List<SessionQuestions> getGameQuestions() {
//        return categoryQuestionsMap.values().stream()
//                .flatMap(List::stream)
//                .collect(Collectors.toList());
//    }

    // Helper method to set questions from a flat list (for backward compatibility)
    public void setGameQuestions(List<SessionQuestions> gameQuestions) {
        // Group questions by category
        this.categoryQuestionsMap = gameQuestions.stream()
                .collect(Collectors.groupingBy(SessionQuestions::getCategoryId));
    }
}