
package com.careerpilot.backendcodebase.controller;

import com.careerpilot.backendcodebase.dtos.*;
import com.careerpilot.backendcodebase.service.AttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempt")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startAttempt(
            @RequestBody StartAttemptDTO startAttemptDTO) {

        try {
            return ResponseEntity.ok(
                    attemptService.startAttempt(startAttemptDTO)
            );

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<AttemptResultResponseDTO> submitAttempt(
            @RequestBody SubmitAttemptDTO submitAttemptDTO) {

        AttemptResultResponseDTO result =
                attemptService.submitAttempt(submitAttemptDTO);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{attemptId}/answer")
    public ResponseEntity<String> saveAnswer(
            @PathVariable Long attemptId,
            @RequestBody SaveAnswerDTO dto) {

        attemptService.saveAnswer(attemptId, dto);

        return ResponseEntity.ok("Answer saved successfully.");
    }

    @GetMapping("/{attemptId}/answers")
    public ResponseEntity<List<SavedAnswerResponseDTO>> getSavedAnswers(
            @PathVariable Long attemptId) {

        return ResponseEntity.ok(
                attemptService.getSavedAnswers(attemptId)
        );
    }

    @GetMapping("/active/{userId}")
    public ResponseEntity<StartAttemptResponseDTO> getActiveAttempt(
            @PathVariable Long userId,
            @RequestParam String testType,
            @RequestParam String sourceName) {

        StartAttemptResponseDTO activeAttempt =
                attemptService.getActiveAttempt(
                        userId,
                        testType,
                        sourceName
                );

        if (activeAttempt == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(activeAttempt);
    }

    @GetMapping("/{attemptId}/result")
    public ResponseEntity<AttemptResultResponseDTO> getAttemptResult(
            @PathVariable Long attemptId) {

        return ResponseEntity.ok(
                attemptService.getAttemptResult(attemptId)
        );
    }

    @GetMapping("/mock-status/{userId}")
    public ResponseEntity<List<MockTestStatusDTO>> getMockTestStatuses(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                attemptService.getMockTestStatuses(userId)
        );
    }


}
