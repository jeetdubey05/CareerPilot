package com.careerpilot.backendcodebase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mock_attempts", indexes = {
        @Index(name = "idx_attempt_user", columnList = "user_id"),
        @Index(name = "idx_attempt_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String status; // "IN_PROGRESS", "COMPLETED", "EXPIRED", "ABANDONED"

    private String testType;   // "COMPANY_MOCK", "TOPIC_PRACTICE", "CUSTOM_FILTER"
    private String sourceName; // "TCS", "Data Structures", "Quantitative"

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer totalQuestions;
    private Integer totalQuestionsAttempted;
    private Integer correctAnswers;

    private Double scorePercentage;
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "mockAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserAnswer> userAnswers = new ArrayList<>();


}