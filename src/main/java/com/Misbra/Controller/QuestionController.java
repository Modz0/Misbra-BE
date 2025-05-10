package com.Misbra.Controller;

import com.Misbra.DTO.QuestionDTO;
import com.Misbra.Entity.User;
import com.Misbra.Enum.Difficulty;
import com.Misbra.Enum.QuestionType;
import com.Misbra.Enum.RoleEnum;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Service.QuestionService;
import com.Misbra.Utils.AuthMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ExceptionUtils exceptionUtils ;

    @Autowired
    public QuestionController(QuestionService questionService, ExceptionUtils exceptionUtils) {
        this.questionService = questionService;
        this.exceptionUtils = exceptionUtils;
    }

    @GetMapping
    public ResponseEntity<Page<QuestionDTO>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) List<String> selectedCategory,
            @RequestParam(required = false) List<Difficulty> selectedDifficulty,
            @RequestParam(required = false) List<QuestionType> selectedQuestionType,
            @RequestParam(required = false) List<Boolean> selectedVerificationStatus

    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(questionService.getAllQuestions(pageable,selectedCategory,selectedDifficulty,selectedQuestionType,selectedVerificationStatus));
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

//    @GetMapping("/unanswered")
//    public ResponseEntity<List<QuestionDTO>> getUnansweredQuestionsByCategory(
//            @RequestParam String userId,
//            @RequestParam String category,
//            @RequestParam(defaultValue = "10") int limit,
//    @RequestParam Difficulty difficulty) {
//        return ResponseEntity.ok(questionService.getUnansweredQuestionsByCategory(userId, category, limit,difficulty));
//    }
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

    @PutMapping("/{id}/approve")
    public ResponseEntity<QuestionDTO> approveQuestion(@AuthenticationPrincipal User user,@PathVariable String id) {
        if(!user.getRole().equals(RoleEnum.ADMIN)){
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    AuthMessageKeys.NOT_AUTHORIZED,
                    new String[]{user.getPhone()}
            ));
            exceptionUtils.throwValidationException(errors);
        }
        QuestionDTO approvedQuestion = questionService.approveQuestion(id);
        return ResponseEntity.ok(approvedQuestion);
    }



}