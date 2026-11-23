package com.careerpilot.backendcodebase.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class SavedAnswerResponseDTO {
    private Long questionId;
    private Long selectedOptionId;
    private Integer timeTakenSeconds;
}
