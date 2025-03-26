package com.Misbra.Entity;

import com.Misbra.Enum.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {
    @Id
    private String questionId;
    private String category;
    private String question;
    private String answer;
    private Difficulty difficulty;
    private int points;
    private boolean isAnswered;
    private String questionPhotoId;
    private String answerPhotoId;
}