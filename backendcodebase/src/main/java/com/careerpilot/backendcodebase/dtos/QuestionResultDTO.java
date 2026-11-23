package com.careerpilot.backendcodebase.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResultDTO {
    private Long questionId;
    private Integer questionNumber;
    private String questionText;

    private List<OptionResultDTO> options;

    private Long selectedOptionId;
    private Long correctOptionId;

    private String answerStatus;
    private String explanation;
}
