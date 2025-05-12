package com.Misbra.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReportDTO {

    private String id;
    private String questionId;
    private String userId;
    private String reportMessage;
    private boolean reviewed = false;
    private LocalDateTime createdAt;
}