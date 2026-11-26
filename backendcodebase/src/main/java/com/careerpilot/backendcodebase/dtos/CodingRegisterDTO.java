package com.careerpilot.backendcodebase.dtos;

import com.careerpilot.backendcodebase.entity.DifficultyLevel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CodingRegisterDTO {

    private String number;
    private String  questionName;
    private String url;
    private DifficultyLevel difficultyLevel;
    private String description;


}

