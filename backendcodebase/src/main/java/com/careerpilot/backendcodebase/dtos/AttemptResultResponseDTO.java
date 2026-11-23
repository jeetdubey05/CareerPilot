package com.careerpilot.backendcodebase.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttemptResultResponseDTO {
    private Long attemptId;
    private String status;
    private Integer totalQuestions;
    private Integer totalQuestionsAttempted;
    private Integer correctAnswers;
    private Double scorePercentage;
    private String feedbackMessage;
    private Integer wrongAnswers;
    private Integer unattemptedQuestions;
    private List<QuestionResultDTO> questionResults;
}
