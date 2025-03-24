package com.Misbra.Service;

import com.Misbra.DTO.QuestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface QuestionService {
    List<QuestionDTO> getAllQuestions();

    QuestionDTO getQuestionById(String id);

    QuestionDTO createQuestion(QuestionDTO questionDTO);

    QuestionDTO updateQuestion(QuestionDTO questionDTO);

    void deleteQuestion(String id);

    Page<QuestionDTO> getQuestionsByCategory(String category, Pageable pageable);


    List<QuestionDTO> getUnansweredQuestionsByCategory(String userId, String category, int limit, String difficulty);


}