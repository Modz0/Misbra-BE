package com.Misbra.Service;

import com.Misbra.DTO.QuestionDTO;
import com.Misbra.Enum.Difficulty;
import com.Misbra.Enum.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface QuestionService {
    Page<QuestionDTO> getAllQuestions(  Pageable pageable ,List<String> selectedCategory, List<Difficulty> selectedDifficulty, List<QuestionType> questionTypes,List<Boolean> selectedVerificationStatus);

    QuestionDTO getQuestionById(String id);

    QuestionDTO createQuestion(QuestionDTO questionDTO);

    QuestionDTO updateQuestion(QuestionDTO questionDTO);

    void deleteQuestion(String id);

    Page<QuestionDTO> getQuestionsByCategory(String category, Pageable pageable);


    List<QuestionDTO> getUnansweredQuestionsByCategory(String userId, String category, int limit, Difficulty difficulty, QuestionType questionType,Boolean isVerified);

    int calculateAvailableGamesCount(String categoryId, String userId , QuestionType questionType,Boolean isVerified);

    void clearQuestionRecord(String userId);
    String uploadQuestionPhotos(String questionId, MultipartFile file) throws IOException;

    String uploadAnswerPhotos(String questionId, MultipartFile file) throws IOException;

    void setQuestionThumbnail(String questionId, String photoId);
    void setAnswerThumbnail(String questionId, String photoId);

    List<QuestionDTO> findQuestionByCategory (String category);
    QuestionDTO approveQuestion(String questionId);

}