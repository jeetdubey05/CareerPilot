package com.careerpilot.backendcodebase.controller;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class AiResumeController {

    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    private final ChatClient chatClient;
    private final Tika tika = new Tika();

    public AiResumeController(GoogleGenAiChatModel googleGenAiChatModel) {
        this.chatClient = ChatClient.create(googleGenAiChatModel);
    }

    public record JobDescriptionPayload(String role, String description) {}

    public record ResumeAnalysisResponse(
            String candidateName,
            String detectedSkills,
            String status,
            String targetedRole,
            int atsScore,
            String scoreLabel,
            List<String> strengths,
            List<String> improvements,
            JdMatch jdMatch,
            List<SkillMatch> skillMatches
    ) {
        public record JdMatch(List<String> matchedKeywords, List<String> missingKeywords) {}
        public record SkillMatch(String label, int percent, String color) {}
    }

    @PostMapping(value = "/ats", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeAnalysisResponse analyzer(
            @RequestPart("file") MultipartFile file,
            @RequestPart("jd") JobDescriptionPayload jobData) {

        validateInputs(file, jobData);

        String fileContent = extractResumeText(file);

        String targetRole = (jobData.role() == null || jobData.role().isBlank())
                ? "Software Engineer"
                : jobData.role().trim();

        String userPayload = """
                Target Role: %s
                Job Description: %s
                Candidate Resume: %s
                """.formatted(targetRole, jobData.description().trim(), fileContent);

        try {
            ResumeAnalysisResponse analysis = chatClient.prompt()
                    .system("""
                            You are an ATS resume evaluator for college placements.
                            Analyze the resume against the job description.
                            Set status to "Parsed Successfully".
                            Set scoreLabel based on atsScore (e.g., "Strong Match", "Good Match", "Needs Improvement").
                            Provide exactly 3 items in skillMatches with labels "Overall JD Match", "Technical Requirements", and "Soft Skills & Leadership", using Tailwind colors "bg-indigo-500", "bg-teal-400", and "bg-blue-500".
                            """)
                    .user(userPayload)
                    .call()
                    .entity(ResumeAnalysisResponse.class);

            if (analysis == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI service returned an empty analysis response"
                );
            }

            return analysis;

        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,"Cannot generate Right now please try again later");
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to analyze resume with AI service: Cannot generate Right now please try again later  "
            );
        }
    }

    private void validateInputs(MultipartFile file, JobDescriptionPayload jobData) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("Resume file must be 5 MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported resume file type: " + contentType);
        }
        if (jobData == null || jobData.description() == null || jobData.description().isBlank()) {
            throw new IllegalArgumentException("Job description is required");
        }
    }

    private String extractResumeText(MultipartFile file) {
        try {
            String extracted = tika.parseToString(file.getInputStream());
            if (extracted == null || extracted.isBlank()) {
                throw new IllegalArgumentException(
                        "Could not extract readable text from resume (file may be scanned/image-only or empty)"
                );
            }
            return extracted.trim();
        } catch (IOException | TikaException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Failed to parse resume file",
                    e
            );
        }
    }
}