package com.careerpilot.backendcodebase.dtos;

import com.careerpilot.backendcodebase.entity.DifficultyLevel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodingResponseDTO {
    private Long Id;
    private String number;
    private String  questionName;
    private String url;
    private DifficultyLevel difficultyLevel;
    private String description;

}
