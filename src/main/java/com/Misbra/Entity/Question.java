package com.Misbra.Entity;

import com.Misbra.Enum.Difficulty;
import com.Misbra.Enum.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
@CompoundIndex(name = "category_difficulty_questionType_idx", def = "{'category': 1, 'difficulty': 1, 'questionType': 1}")
public class Question {
    @Id
    private String questionId;

    @Indexed
    private String category;

    @Indexed
    private Difficulty difficulty;

    private String question;
    private String answer;
    private int points;
    private String questionPhotoId;
    private String answerPhotoId;
    private QuestionType questionType;

    private int answerRange;
    private boolean verified = false;


    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}