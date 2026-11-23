
package com.careerpilot.backendcodebase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "mock_test_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_mock_test",
                        columnNames = {"user_id", "mock_test_id"}
                ),
                @UniqueConstraint(
                        name = "uk_mock_attempt",
                        columnNames = {"attempt_id"}
                )
        },
        indexes = {
                @Index(name = "idx_mock_mapping_user", columnList = "user_id"),
                @Index(name = "idx_mock_mapping_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mock_test_id", nullable = false, length = 100)
    private String mockTestId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private MockAttempt mockAttempt;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
