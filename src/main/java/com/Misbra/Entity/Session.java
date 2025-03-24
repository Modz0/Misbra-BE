package com.Misbra.Entity;

import com.Misbra.Component.SessionQuestions;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "sessions")
public class Session {
    private String sessionId;
    private String userId;
    private String team1Id;
    private String team2Id;
    private int  team1score;
    private int team2score;
    private List<String> gameCategories;
    private List<SessionQuestions> gameQuestions;
}
