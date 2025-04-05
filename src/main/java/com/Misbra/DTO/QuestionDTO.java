package com.Misbra.DTO;

import com.Misbra.Enum.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private String questionId;
    private String category;
    private String question;
    private String answer; // Only included in responses for answered questions
    private Difficulty difficulty;
    private int points;
    private String questionPhotoId;
    private String answerPhotoId;
    private String questionThumbnailUrl;
    private String answerThumbnailUrl;

    // For client responses (doesn't include correct answer)
    public QuestionDTO hideAnswer() {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionId(this.questionId);
        dto.setCategory(this.category);
        dto.setQuestion(this.question);
        dto.setDifficulty(this.difficulty);
        dto.setPoints(this.points);
        // Notice answer is not set
        return dto;
    }
}