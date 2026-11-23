
package com.careerpilot.backendcodebase.repository;

import com.careerpilot.backendcodebase.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAnswerRepository
        extends JpaRepository<UserAnswer, Long> {

//  Find an existing answer for a particular question in an attempt.....
    Optional<UserAnswer> findByMockAttempt_IdAndQuestion_QuestionId(
            Long attemptId,
            Long questionId
    );

//=======================================================================================================

  //fetch all the answer selected by user after refresh or reconnection....
    List<UserAnswer> findByMockAttempt_Id(Long attemptId);
}
