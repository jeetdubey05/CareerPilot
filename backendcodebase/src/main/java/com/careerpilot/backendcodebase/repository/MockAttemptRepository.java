
package com.careerpilot.backendcodebase.repository;

import com.careerpilot.backendcodebase.entity.MockAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockAttemptRepository extends JpaRepository<MockAttempt, Long> {

    List<MockAttempt> findByUser_UserId(Long userId);

    List<MockAttempt> findByUser_UserIdAndStatus(
            Long userId,
            String status
    );

    Optional<MockAttempt>
    findFirstByUser_UserIdAndTestTypeAndSourceNameAndStatusOrderByStartTimeDesc(
            Long userId,
            String testType,
            String sourceName,
            String status
    );

    List<MockAttempt> findByUser_UserIdAndStatusAndTestType(
            Long userId,
            String status,
            String testType
    );
}
