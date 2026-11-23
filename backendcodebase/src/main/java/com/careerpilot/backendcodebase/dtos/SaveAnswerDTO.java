package com.careerpilot.backendcodebase.dtos;

import lombok.Data;

@Data
public class SaveAnswerDTO {
    private Long questionId;
    private Long selectedOptionId;
    private Integer timeTakenSeconds;
}
