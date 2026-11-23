package com.careerpilot.backendcodebase.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionResultDTO {
    private Long optionId;
    private String optionText;
    private boolean correct;
}