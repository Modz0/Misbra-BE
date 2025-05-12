package com.Misbra.Service;

import com.Misbra.DTO.QuestionReportDTO;
import com.Misbra.Entity.QuestionReport;
import com.Misbra.Exception.Utils.ExceptionUtils;
import com.Misbra.Exception.Validation.ValidationErrorDTO;
import com.Misbra.Mapper.QuestionReportMapper;
import com.Misbra.Repository.QuestionReportRepository;
import com.Misbra.Repository.QuestionRepository;
import com.Misbra.Repository.UserRepository;
import com.Misbra.Utils.BusinessMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionReportServiceImp implements QuestionReportService {

    private final QuestionReportRepository reportRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ExceptionUtils exceptionUtils;
    private final QuestionReportMapper reportMapper;

    @Autowired
    public QuestionReportServiceImp(
            QuestionReportRepository reportRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository,
            ExceptionUtils exceptionUtils,
            QuestionReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.exceptionUtils = exceptionUtils;
        this.reportMapper = reportMapper;
    }

    public QuestionReportDTO createReport(String questionId, String reporterId, String reportMessage) {
        // Check if question exists
        if (!questionRepository.existsById(questionId)) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.QUESTION_NOT_FOUND,
                    new String[]{questionId}
            ));
             exceptionUtils.throwValidationException(errors);
        }

        // Check if user exists
        if (!userRepository.existsById(reporterId)) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.INVALID_INPUT,
                    new String[]{"User not found with ID: " + reporterId}
            ));
             exceptionUtils.throwValidationException(errors);
        }

        // Create and save report
        QuestionReport report = new QuestionReport();
        report.setQuestionId(questionId);
        report.setUserId(reporterId);
        report.setReportMessage(reportMessage);
        report.setReviewed(false);
        report.setCreatedAt(LocalDateTime.now());

        QuestionReport savedReport = reportRepository.save(report);

        return reportMapper.toDTO(savedReport);
    }

    public Page<QuestionReportDTO> getPendingReports(Pageable pageable) {
        return reportRepository.findByReviewed(false, pageable)
                .map(reportMapper::toDTO);
    }

    public QuestionReportDTO markAsReviewed(String reportId) {
        QuestionReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> {
                    List<ValidationErrorDTO> errors = new ArrayList<>();
                    errors.add(new ValidationErrorDTO(
                            BusinessMessageKeys.REPORT_NOT_FOUND,
                            new String[]{reportId}
                    ));
                     exceptionUtils.throwValidationException(errors);
                     return null;
                });

        if (report.isReviewed()) {
            List<ValidationErrorDTO> errors = new ArrayList<>();
            errors.add(new ValidationErrorDTO(
                    BusinessMessageKeys.REPORT_ALREADY_REVIEWED,
                    new String[]{}
            ));
             exceptionUtils.throwValidationException(errors);
        }

        report.setReviewed(true);

        return reportMapper.toDTO(reportRepository.save(report));
    }
}