package com.Misbra.Component;

import lombok.Data;

@Data
public class SessionQuestions{
    private String questionId;
    private String categoryId;
    private boolean answered;
    private String answeredByTeam;
    private boolean answeredCorrectly;
    private int pointsAwarded;
}

