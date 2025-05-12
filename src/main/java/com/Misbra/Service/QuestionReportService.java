package com.Misbra.Service;

import com.Misbra.DTO.QuestionReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionReportService {

    /**
     * Create a new question report
     *
     * @param questionId ID of the question being reported
     * @param reporterId ID of the user reporting the question
     * @param reportMessage Message containing the suggested answer or difficulty
     * @return The created report DTO
     */
    QuestionReportDTO createReport(String questionId, String reporterId, String reportMessage);

    /**
     * Get all reports that have not been reviewed yet
     *
     * @param pageable Pagination information
     * @return Page of pending reports
     */
    Page<QuestionReportDTO> getPendingReports(Pageable pageable);

    /**
     * Mark a report as reviewed
     *
     * @param reportId ID of the report to mark as reviewed
     * @return The updated report DTO
     */
    QuestionReportDTO markAsReviewed(String reportId);
}