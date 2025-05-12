package com.Misbra.Controller;

import com.Misbra.DTO.QuestionReportDTO;
import com.Misbra.Entity.User;
import com.Misbra.Enum.RoleEnum;
import com.Misbra.Service.QuestionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question-reports")
public class QuestionReportController {

    private final QuestionReportService reportService;

    @Autowired
    public QuestionReportController(QuestionReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/question/{questionId}")
    public ResponseEntity<QuestionReportDTO> reportQuestion(
            @PathVariable String questionId,
            @RequestBody String reportMessage,
            @AuthenticationPrincipal User user) {

        QuestionReportDTO report = reportService.createReport(questionId, user.getUserId(), reportMessage);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<QuestionReportDTO>> getPendingReports(
            Pageable pageable,
            @AuthenticationPrincipal User user) {

        // Only admins can see pending reports
        if (!user.getAuthorities().equals(RoleEnum.ADMIN.getDescription())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        return ResponseEntity.ok(reportService.getPendingReports(pageable));
    }

    @PutMapping("/{id}/mark-reviewed")
    public ResponseEntity<QuestionReportDTO> markAsReviewed(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {

        // Only admins can mark reports as reviewed
        if (!user.getAuthorities().equals(RoleEnum.ADMIN.getDescription())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        return ResponseEntity.ok(reportService.markAsReviewed(id));
    }
}