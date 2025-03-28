package com.Misbra.Component;

import com.Misbra.Enum.Difficulty;
import lombok.Data;

@Data
public class SessionQuestions{
    private String questionId;
    private String categoryId;
    private boolean answered;
    private String answeredByTeam;
    private int pointsAwarded;
    private Difficulty difficulty;
}

