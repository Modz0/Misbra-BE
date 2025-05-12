package com.Misbra.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "question_reports")
public class QuestionReport {

    @Id
    private String id;


    private String questionId;


    private String userId;

    private String reportMessage;

    private boolean reviewed = false;

    @CreatedDate
    private LocalDateTime createdAt;
}