package com.careerpilot.backendcodebase.dtos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartAttemptResponseDTO {
    private Long attemptId;
    private Integer totalQuestions;
    private LocalDateTime expiresAt;
    private List<AttemptQuestionResponseDTO> questions;
}
