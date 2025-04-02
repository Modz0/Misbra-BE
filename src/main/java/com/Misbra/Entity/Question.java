package com.Misbra.Entity;

import com.Misbra.Enum.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {
    @Id
    private String questionId;  // Already indexed automatically

    @Indexed
    private String category;    // Important for category-based queries

    @Indexed
    private Difficulty difficulty;  // For difficulty-based filtering

    @Indexed
    private boolean isAnswered;  // To quickly find answered/unanswered questions


    // Other fields that don't need indexing
    private String question;
    private String answer;
    private int points;
    private String questionPhotoId;
    private String answerPhotoId;
}