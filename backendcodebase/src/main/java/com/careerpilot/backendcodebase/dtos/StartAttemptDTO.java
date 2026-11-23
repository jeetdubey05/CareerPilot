package com.careerpilot.backendcodebase.dtos;

import com.careerpilot.backendcodebase.entity.QuestionCategory;
import lombok.Data;

import java.util.List;

@Data
public class StartAttemptDTO {

    private Long userId;
    private String testType;
    private String sourceName;
    private Integer numberOfQuestions;
    private Long companyId;
    private List<QuestionCategory> categories;
    private String mockTestId;
}