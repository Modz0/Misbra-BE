package com.Misbra.Controller;

import com.Misbra.DTO.QuestionDTO;
import com.Misbra.Entity.User;
import com.Misbra.Enum.Difficulty;
import com.Misbra.Service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable String id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(@RequestBody QuestionDTO questionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(questionDTO));
    }

    @PutMapping
    public ResponseEntity<QuestionDTO> updateQuestion(@RequestBody QuestionDTO questionDTO) {
        return ResponseEntity.ok(questionService.updateQuestion(questionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<QuestionDTO>> getQuestionsByCategory(
            @PathVariable String category, Pageable pageable) {
        return ResponseEntity.ok(questionService.getQuestionsByCategory(category, pageable));
    }

    @GetMapping("/unanswered")
    public ResponseEntity<List<QuestionDTO>> getUnansweredQuestionsByCategory(
            @RequestParam String userId,
            @RequestParam String category,
            @RequestParam(defaultValue = "10") int limit,
    @RequestParam Difficulty difficulty) {
        return ResponseEntity.ok(questionService.getUnansweredQuestionsByCategory(userId, category, limit,difficulty));
    }
    @PostMapping("/{questionId}/add-photo")
    public ResponseEntity<?> uploadQuestionPhotos(
            @PathVariable String questionId,
            @RequestParam("question-file") MultipartFile qFile,
            @RequestParam("answer-file") MultipartFile aFile
    ) {
        try {
            Map<String, String> result = new HashMap<>();

            if (!qFile.isEmpty()) {
                String questionImageUrl = questionService.uploadQuestionPhotos(questionId, qFile);
                result.put("questionImageUrl", questionImageUrl);
            }
            if (!aFile.isEmpty()) {
                String answerImageUrl = questionService.uploadAnswerPhotos(questionId, aFile);
                result.put("answerImageUrl", answerImageUrl);
            }

            return ResponseEntity.ok(result.isEmpty() ? "OK" : result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }
    @PostMapping("/clear-record")
    public ResponseEntity<?> clearQuestionRecord(@AuthenticationPrincipal User user) {
        questionService.clearQuestionRecord(user.getUserId());
        return ResponseEntity.ok("OK");
    }


}