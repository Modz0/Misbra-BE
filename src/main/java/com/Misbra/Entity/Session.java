package com.Misbra.Entity;

import com.Misbra.Component.SessionQuestions;
import com.Misbra.Component.TeamPowerup;
import com.Misbra.Enum.PowerupType;
import com.Misbra.Enum.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private Map<String, Integer> powerups;
    private Map<String, List<SessionQuestions>> categoryQuestionsMap = new HashMap<>();
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    private List<TeamPowerup> team1Powerups = new ArrayList<>();
    private List<TeamPowerup> team2Powerups = new ArrayList<>();


    // Initialize powerups
    public void initializePowerups() {
        team1Powerups.clear();
        team2Powerups.clear();

        // Add one of each powerup type to each team
        for (PowerupType type : PowerupType.values()) {
            team1Powerups.add(new TeamPowerup(type, false, false));
            team2Powerups.add(new TeamPowerup(type, false, false));
        }
    }

}