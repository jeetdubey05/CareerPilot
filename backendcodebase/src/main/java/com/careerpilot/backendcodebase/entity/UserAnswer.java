package com.careerpilot.backendcodebase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_answers",
        indexes = {
                @Index(name = "idx_answer_attempt", columnList = "attempt_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attempt_question",
                        columnNames = {"attempt_id", "question_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private MockAttempt mockAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private Options selectedOption;

    private Boolean isCorrect;
    private Integer timeTakenSeconds;
}