
package com.careerpilot.backendcodebase.repository;

import com.careerpilot.backendcodebase.entity.MockTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockTestAttemptRepository
        extends JpaRepository<MockTestAttempt, Long> {

    Optional<MockTestAttempt> findByUser_UserIdAndMockTestId(
            Long userId,
            String mockTestId
    );

    Optional<MockTestAttempt> findByMockAttempt_Id(Long attemptId);

    List<MockTestAttempt> findByUser_UserId(Long userId);

    List<MockTestAttempt> findByUser_UserIdAndStatus(
            Long userId,
            String status
    );

    long countByUser_UserIdAndStatus(Long userId, String status);
}
