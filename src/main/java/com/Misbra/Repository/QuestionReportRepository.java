package com.Misbra.Repository;

import com.Misbra.Entity.QuestionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionReportRepository extends MongoRepository<QuestionReport, String> {

    List<QuestionReport> findByQuestionId(String questionId);

    Page<QuestionReport> findByReviewed(boolean reviewed, Pageable pageable);
}