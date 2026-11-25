package com.careerpilot.backendcodebase.controller;



import org.apache.tika.Tika;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/resume")

public class AiResumeController {

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
    )
    {
        public record JdMatch(List<String> matchedKeywords, List<String> missingKeywords) {}
        public record SkillMatch(String label, int percent, String color) {}
    }

    @PostMapping(value = "/ats", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeAnalysisResponse analyzer(
            @RequestPart("file") MultipartFile file,
            @RequestPart("jd") JobDescriptionPayload jobData) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume file is required");
        }
        if (jobData == null || jobData.description() == null || jobData.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job description is required");
        }

        String fileContent;
        try {
            fileContent = tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to extract resume text");
        }

        String userPayload = """
                Target Role: %s
                Job Description: %s
                Candidate Resume: %s
                """.formatted(
                jobData.role() == null ? "Software Engineer" : jobData.role(),
                jobData.description(),
                fileContent
        );

        return chatClient.prompt()
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
    }
}
