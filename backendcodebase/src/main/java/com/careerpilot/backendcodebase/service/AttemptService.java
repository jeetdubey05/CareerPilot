
package com.careerpilot.backendcodebase.service;

import com.careerpilot.backendcodebase.dtos.*;
import com.careerpilot.backendcodebase.entity.*;
import com.careerpilot.backendcodebase.repository.*;
import com.careerpilot.backendcodebase.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttemptService {
    private final MockTestAttemptRepository mockTestAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final MockAttemptRepository mockAttemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OptionsRepository optionsRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final QuestionSelectionService questionSelectionService;
    private final CompanyRepository companyRepository;

    public AttemptService(
            MockAttemptRepository mockAttemptRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            OptionsRepository optionsRepository,
            AttemptQuestionRepository attemptQuestionRepository,
            QuestionSelectionService questionSelectionService, CompanyRepository companyRepository,
            UserAnswerRepository userAnswerRepository,
            MockTestAttemptRepository mockTestAttemptRepository ) {

        this.mockAttemptRepository = mockAttemptRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.optionsRepository = optionsRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.questionSelectionService = questionSelectionService;
        this.companyRepository = companyRepository;
        this.userAnswerRepository=userAnswerRepository;
        this.mockTestAttemptRepository=mockTestAttemptRepository;
    }
// ------------------------------------ start Attempt ------------------------------------------------


    @Transactional
    public StartAttemptResponseDTO startAttempt(StartAttemptDTO dto) {

        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        if (dto.getNumberOfQuestions() == null
                || dto.getNumberOfQuestions() <= 0) {
            throw new IllegalArgumentException(
                    "Number of questions must be greater than zero."
            );
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + dto.getUserId()
                ));

        if (dto.getMockTestId() == null || dto.getMockTestId().isBlank()) {
            throw new IllegalArgumentException("Mock test ID is required.");
        }

        // Determine test type and source before checking active attempts.
        Company company = null;

        String testType;
        String sourceName;

        if (dto.getCompanyId() == null) {

            testType = dto.getTestType() != null
                    ? dto.getTestType()
                    : "GENERAL";

            sourceName = dto.getSourceName() != null
                    ? dto.getSourceName()
                    : "PRACTICE";

        } else {

            company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Company not found with id: " + dto.getCompanyId()
                    ));

            testType = "COMPANY_PYQ";
            sourceName = company.getName();
        }

        // Prevent reattempting a completed predefined mock.
        if (isPredefinedMock(testType, sourceName)) {

            boolean alreadyCompleted =
                    mockTestAttemptRepository
                            .findByUser_UserIdAndMockTestId(
                                    dto.getUserId(),
                                    dto.getMockTestId()
                            )

                            .filter(mapping ->
                                    "COMPLETED".equals(mapping.getStatus()))
                            .isPresent();

            if (alreadyCompleted) {
                throw new IllegalStateException(
                        "This mock test has already been completed."
                );
            }
        }

        // Check only the active attempt for THIS particular test.
        MockAttempt activeAttempt = mockAttemptRepository
                .findFirstByUser_UserIdAndTestTypeAndSourceNameAndStatusOrderByStartTimeDesc(
                        dto.getUserId(),
                        testType,
                        sourceName,
                        "IN_PROGRESS"
                )
                .orElse(null);

        LocalDateTime currentTime = LocalDateTime.now();

        if (activeAttempt != null) {

            // Expire the existing attempt if its deadline has passed.
            if (activeAttempt.getExpiresAt() != null
                    && !activeAttempt.getExpiresAt().isAfter(currentTime)) {

                activeAttempt.setStatus("EXPIRED");
                activeAttempt.setEndTime(currentTime);

                mockAttemptRepository.save(activeAttempt);

            } else {

                throw new IllegalStateException(
                        "You already have an active attempt for this test. Please resume or submit it before starting another."
                );
            }
        }

        // Select questions.
        List<Question> selectedQuestions;

        if (company == null) {

            selectedQuestions =
                    questionSelectionService.selectQuestions(
                            dto.getNumberOfQuestions(),
                            dto.getCategories()
                    );

        } else {

            selectedQuestions =
                    questionSelectionService.selectCompanyQuestions(
                            company.getCompanyId(),
                            dto.getNumberOfQuestions()
                    );
        }

        // Validate question availability.
        if (selectedQuestions == null
                || selectedQuestions.size() < dto.getNumberOfQuestions()) {

            int available = selectedQuestions == null
                    ? 0 : selectedQuestions.size();

            throw new IllegalStateException(
                    "Not enough questions available. Available: "
                            + available
                            + ", requested: "
                            + dto.getNumberOfQuestions()
            );
        }

        // Set start time and deadline.
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime expiresAt = startTime.plusMinutes(20);

        // Create new attempt.
        MockAttempt attempt = MockAttempt.builder()
                .user(user)
                .status("IN_PROGRESS")
                .testType(testType)
                .sourceName(sourceName)
                .startTime(startTime)
                .expiresAt(expiresAt)
                .totalQuestions(selectedQuestions.size())
                .totalQuestionsAttempted(0)
                .correctAnswers(0)
                .build();

        MockAttempt savedAttempt = mockAttemptRepository.save(attempt);

        // Save predefined mock-to-attempt relationship.
        if (isPredefinedMock(testType, sourceName)) {
            createMockMapping(user, dto.getMockTestId(), savedAttempt);
        }
        // Assign questions in order.
        int order = 1;

        for (Question question : selectedQuestions) {

            AttemptQuestion assignedQuestion = new AttemptQuestion();

            assignedQuestion.setMockAttempt(savedAttempt);
            assignedQuestion.setQuestion(question);
            assignedQuestion.setQuestionOrder(order++);

            attemptQuestionRepository.save(assignedQuestion);
        }

        // Prepare response.
        List<AttemptQuestionResponseDTO> questions =
                mapToAttemptQuestionDTOs(selectedQuestions, 1);

        return StartAttemptResponseDTO.builder()
                .attemptId(savedAttempt.getId())
                .totalQuestions(selectedQuestions.size())
                .expiresAt(savedAttempt.getExpiresAt())
                .questions(questions)
                .build();
    }


    // -------------------------- fetching question method-------------------------------------
    // FETCH QUESTIONS FOR AN EXISTING ATTEMPT
    @Transactional(readOnly = true)
    public List<AttemptQuestionResponseDTO> getAttemptQuestions(
            Long attemptId) {

        MockAttempt attempt = mockAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException(
                        "Attempt not found with id: " + attemptId
                ));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException(
                    "Attempt is not in progress."
            );
        }

        List<AttemptQuestion> assignedQuestions =
                attemptQuestionRepository
                        .findByMockAttempt_IdOrderByQuestionOrderAsc(
                                attemptId
                        );

        return assignedQuestions.stream()
                .map(assigned -> {
                    Question question = assigned.getQuestion();

                    return AttemptQuestionResponseDTO.builder()
                            .questionId(question.getQuestionId())
                            .questionOrder(assigned.getQuestionOrder())
                            .question(question.getQuestion())
                            .options(mapOptions(question))
                            .build();
                })
                .toList();
    }

    // MAP QUESTIONS TO RESPONSE DTOs
    private List<AttemptQuestionResponseDTO> mapToAttemptQuestionDTOs(
            List<Question> questions,
            int startingOrder) {

        int[] order = {startingOrder};

        return questions.stream()
                .map(question ->
                        AttemptQuestionResponseDTO.builder()
                                .questionId(question.getQuestionId())
                                .questionOrder(order[0]++)
                                .question(question.getQuestion())
                                .options(mapOptions(question))
                                .build()
                )
                .toList();
    }

    // MAP OPTIONS WITHOUT EXPOSING CORRECT ANSWERS
    private List<optionResponseDTO> mapOptions(Question question) {

        return question.getOptions().stream()
                .map(option -> new optionResponseDTO(
                        option.getId(),
                        option.getOption()
                ))
                .toList();
    }
    //------------------------------Save Answer---------------------------------------------------------------

    @Transactional
    public void saveAnswer(Long attemptId, SaveAnswerDTO dto) {

        // 1. Validate request
        if (dto.getQuestionId() == null) {
            throw new IllegalArgumentException("Question ID is required.");
        }

        if (dto.getTimeTakenSeconds() != null
                && dto.getTimeTakenSeconds() < 0) {
            throw new IllegalArgumentException("Invalid time taken.");
        }

        // 2. Fetch attempt
        MockAttempt attempt = mockAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found."));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException("Attempt is not in progress.");
        }

        // 3. Check expiry
        if (attempt.getExpiresAt() != null
                && !LocalDateTime.now().isBefore(attempt.getExpiresAt())) {
            throw new IllegalStateException("Attempt has expired.");
        }

        // 4. Verify question belongs to this attempt
        List<AttemptQuestion> assignedQuestions =
                attemptQuestionRepository
                        .findByMockAttempt_IdOrderByQuestionOrderAsc(attemptId);

        Question question = assignedQuestions.stream()
                .map(AttemptQuestion::getQuestion)
                .filter(q -> Objects.equals(
                        q.getQuestionId(), dto.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question does not belong to this attempt."));

        // 5. Validate selected option
        Options selectedOption = null;

        if (dto.getSelectedOptionId() != null) {

            selectedOption = optionsRepository.findById(
                    dto.getSelectedOptionId()
            ).orElseThrow(() -> new RuntimeException("Option not found."));

            if (selectedOption.getQuestion() == null
                    || !Objects.equals(
                    selectedOption.getQuestion().getQuestionId(),
                    dto.getQuestionId())) {

                throw new IllegalArgumentException(
                        "Selected option does not belong to this question.");
            }
        }

        // 6. Check whether answer already exists
        UserAnswer userAnswer = userAnswerRepository
                .findByMockAttempt_IdAndQuestion_QuestionId(
                        attemptId, dto.getQuestionId())
                .orElseGet(() -> UserAnswer.builder()
                        .mockAttempt(attempt)
                        .question(question)
                        .build());

        // 7. Insert or update answer
        userAnswer.setSelectedOption(selectedOption);

        userAnswer.setTimeTakenSeconds(
                dto.getTimeTakenSeconds() != null
                        ? dto.getTimeTakenSeconds() : 0
        );

        // Correctness will be evaluated during final submission.
        userAnswer.setIsCorrect(null);

        userAnswerRepository.save(userAnswer);
    }
    //-----------------------------Get saved Answer on refresh-------------------------------------------------

    @Transactional(readOnly = true)
    public List<SavedAnswerResponseDTO> getSavedAnswers(Long attemptId) {

        MockAttempt attempt = mockAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found."));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException("Attempt is not in progress.");
        }

        return userAnswerRepository.findByMockAttempt_Id(attemptId)
                .stream()
                .map(answer -> SavedAnswerResponseDTO.builder()
                        .questionId(answer.getQuestion().getQuestionId())
                        .selectedOptionId(
                                answer.getSelectedOption() != null
                                        ? answer.getSelectedOption().getId()
                                        : null
                        )
                        .timeTakenSeconds(answer.getTimeTakenSeconds())
                        .build())
                .toList();
    }

//------------------------------GET ACTIVE ATTEMPT-----------------------------------------------------


    @Transactional
    public StartAttemptResponseDTO getActiveAttempt(
            Long userId,
            String testType,
            String sourceName) {

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        if (testType == null || sourceName == null) {
            throw new IllegalArgumentException(
                    "Test type and source name are required."
            );
        }

        // Find active attempt for this particular test.
        MockAttempt attempt = mockAttemptRepository
                .findFirstByUser_UserIdAndTestTypeAndSourceNameAndStatusOrderByStartTimeDesc(
                        userId,
                        testType,
                        sourceName,
                        "IN_PROGRESS"
                )
                .orElse(null);

        if (attempt == null) {
            return null;
        }

        // Check expiry.
        LocalDateTime now = LocalDateTime.now();

        if (attempt.getExpiresAt() != null
                && !now.isBefore(attempt.getExpiresAt())) {

            attempt.setStatus("EXPIRED");
            attempt.setEndTime(now);

            mockAttemptRepository.save(attempt);

            return null;
        }

        // Fetch originally assigned questions in their original order.
        List<AttemptQuestion> assignedQuestions =
                attemptQuestionRepository
                        .findByMockAttempt_IdOrderByQuestionOrderAsc(
                                attempt.getId()
                        );

        // Ensure mapping exists for an active predefined mock.
        if (isPredefinedMock(testType, sourceName)) {

            boolean mappingExists =
                    mockTestAttemptRepository
                            .findByMockAttempt_Id(attempt.getId())
                            .isPresent();

            if (!mappingExists) {

                User user = userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException("User not found."));

                createMockMapping(user, sourceName, attempt);
            }
        }


        List<AttemptQuestionResponseDTO> questions =
                assignedQuestions.stream()
                        .map(assigned -> {

                            Question question = assigned.getQuestion();

                            return AttemptQuestionResponseDTO.builder()
                                    .questionId(question.getQuestionId())
                                    .questionOrder(assigned.getQuestionOrder())
                                    .question(question.getQuestion())
                                    .options(mapOptions(question))
                                    .build();
                        })
                        .toList();

        return StartAttemptResponseDTO.builder()
                .attemptId(attempt.getId())
                .totalQuestions(attempt.getTotalQuestions())
                .expiresAt(attempt.getExpiresAt())
                .questions(questions)
                .build();
    }


    //----------------------------- SUBMIT ATTEMPT------------------------------------------------------------

    @Transactional
    public AttemptResultResponseDTO submitAttempt(
            SubmitAttemptDTO submissionDTO) {

        // 1. Fetch attempt
        MockAttempt attempt = mockAttemptRepository
                .findById(submissionDTO.getAttemptId())
                .orElseThrow(() -> new RuntimeException(
                        "Attempt not found: " + submissionDTO.getAttemptId()
                ));

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException(
                    "Attempt is not in progress or already submitted."
            );
        }

        // 2. Check expiry
        LocalDateTime currentTime = LocalDateTime.now();

        boolean isExpired = attempt.getExpiresAt() != null
                && !currentTime.isBefore(attempt.getExpiresAt());

        // 3. Fetch assigned questions
        List<AttemptQuestion> assignedQuestions =
                attemptQuestionRepository
                        .findByMockAttempt_IdOrderByQuestionOrderAsc(
                                attempt.getId()
                        );

        if (assignedQuestions.isEmpty()) {
            throw new IllegalStateException(
                    "No questions are assigned to this attempt."
            );
        }

        Set<Long> assignedQuestionIds = new HashSet<>();

        for (AttemptQuestion assigned : assignedQuestions) {
            assignedQuestionIds.add(
                    assigned.getQuestion().getQuestionId()
            );
        }

        // 4. Fetch answers already saved in MySQL
        List<UserAnswer> savedAnswers =
                userAnswerRepository.findByMockAttempt_Id(attempt.getId());

        int totalQuestions = assignedQuestions.size();
        int totalQuestionsAttempted = 0;
        int correctCount = 0;

        // 5. Evaluate saved answers
        for (UserAnswer answer : savedAnswers) {

            Long questionId = answer.getQuestion().getQuestionId();

            if (!assignedQuestionIds.contains(questionId)) {
                throw new IllegalStateException(
                        "An answer belongs to a question outside this attempt."
                );
            }

            Options selectedOption = answer.getSelectedOption();

            boolean isCorrect = false;

            if (selectedOption != null) {

                // Verify option-question relationship
                if (selectedOption.getQuestion() == null
                        || !Objects.equals(
                        selectedOption.getQuestion().getQuestionId(),
                        questionId)) {

                    throw new IllegalStateException(
                            "Invalid selected option for question: " + questionId
                    );
                }

                totalQuestionsAttempted++;

                isCorrect = selectedOption.isCorrect();

                if (isCorrect) {
                    correctCount++;
                }
            }

            answer.setIsCorrect(isCorrect);
        }

        // 6. Calculate score
        double scorePercentage =
                ((double) correctCount / totalQuestions) * 100;

        // 7. Update attempt
        attempt.setEndTime(currentTime);

        attempt.setStatus(isExpired ? "EXPIRED" : "COMPLETED");

        attempt.setTotalQuestions(totalQuestions);
        attempt.setTotalQuestionsAttempted(totalQuestionsAttempted);
        attempt.setCorrectAnswers(correctCount);
        attempt.setScorePercentage(scorePercentage);

        userAnswerRepository.saveAll(savedAnswers);
        mockAttemptRepository.save(attempt);

        // Update predefined mock mapping after submission.
        if ("COMPLETED".equals(attempt.getStatus())) {
            updateMockMapping(attempt.getId(), "COMPLETED");
        }

        // 8. Return result
        return AttemptResultResponseDTO.builder()
                .attemptId(attempt.getId())
                .status(attempt.getStatus())
                .totalQuestions(totalQuestions)
                .totalQuestionsAttempted(totalQuestionsAttempted)
                .correctAnswers(correctCount)
                .scorePercentage(scorePercentage)
                .feedbackMessage(
                        scorePercentage >= 60.0
                                ? "Great performance!"
                                : "Keep practicing!"
                )
                .build();
    }
   //==================================Get Attempt Result method============================

    @Transactional(readOnly = true)
    public AttemptResultResponseDTO getAttemptResult(Long attemptId) {

        // 1. Fetch attempt
        MockAttempt attempt = mockAttemptRepository
                .findById(attemptId)
                .orElseThrow(() -> new RuntimeException(
                        "Attempt not found: " + attemptId
                ));

        // 2. Only allow viewing a finished attempt
        if ("IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException(
                    "Result is not available until the attempt is submitted."
            );
        }

        // 3. Fetch all assigned questions in original order
        List<AttemptQuestion> assignedQuestions =
                attemptQuestionRepository
                        .findByMockAttempt_IdOrderByQuestionOrderAsc(attemptId);

        // 4. Fetch saved answers
        List<UserAnswer> savedAnswers =
                userAnswerRepository.findByMockAttempt_Id(attemptId);

        Map<Long, UserAnswer> answerMap = savedAnswers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getQuestionId(),
                        answer -> answer
                ));



        int correctCount = 0;
        int wrongCount = 0;
        int unattemptedCount = 0;

        List<QuestionResultDTO> questionResults = new ArrayList<>();

        // 5. Process every assigned question
        for (AttemptQuestion assigned : assignedQuestions) {

            Question question = assigned.getQuestion();

            UserAnswer userAnswer =
                    answerMap.get(question.getQuestionId());

            Long selectedOptionId = null;
            Long correctOptionId = null;

            String answerStatus;

            List<OptionResultDTO> optionResults = new ArrayList<>();

            // 6. Process all options and identify correct option
            for (Options option : question.getOptions()) {

                if (option.isCorrect()) {
                    correctOptionId = option.getId();
                }

                if (userAnswer != null
                        && userAnswer.getSelectedOption() != null
                        && userAnswer.getSelectedOption().getId()
                        .equals(option.getId())) {

                    selectedOptionId = option.getId();
                }

                optionResults.add(
                        OptionResultDTO.builder()
                                .optionId(option.getId())
                                .optionText(option.getOption())
                                .correct(option.isCorrect())
                                .build()
                );
            }

            // 7. Determine answer status
            if (userAnswer == null || userAnswer.getSelectedOption() == null) {

                answerStatus = "UNATTEMPTED";
                unattemptedCount++;

            } else if (Boolean.TRUE.equals(userAnswer.getIsCorrect())) {

                answerStatus = "CORRECT";
                correctCount++;

            } else {

                answerStatus = "WRONG";
                wrongCount++;
            }

            // 8. Build question result
            questionResults.add(
                    QuestionResultDTO.builder()
                            .questionId(question.getQuestionId())
                            .questionNumber(assigned.getQuestionOrder())
                            .questionText(question.getQuestion())
                            .options(optionResults)
                            .selectedOptionId(selectedOptionId)
                            .correctOptionId(correctOptionId)
                            .answerStatus(answerStatus)
                            .explanation(question.getExplanation())
                            .build()
            );
        }

        // 9. Build complete result
        return AttemptResultResponseDTO.builder()
                .attemptId(attempt.getId())
                .status(attempt.getStatus())
                .totalQuestions(assignedQuestions.size())
                .totalQuestionsAttempted(correctCount + wrongCount)
                .correctAnswers(correctCount)
                .wrongAnswers(wrongCount)
                .unattemptedQuestions(unattemptedCount)
                .scorePercentage(attempt.getScorePercentage())
                .feedbackMessage(
                        attempt.getScorePercentage() >= 60.0
                                ? "Great performance!"
                                : "Keep practicing!"
                )
                .questionResults(questionResults)
                .build();
    }
//===============================Helper MEthod==============================================
// Identify predefined CareerPilot mock tests.
private boolean isPredefinedMock(String testType, String sourceName) {

    return "GENERAL".equals(testType)
            && sourceName != null
            && sourceName.matches("[A-Z]+_MOCK_\\d+");
}


    // Create mapping between predefined mock and generated attempt.
    private void createMockMapping(
            User user,
            String mockTestId,
            MockAttempt attempt
    ) {

        MockTestAttempt mapping = MockTestAttempt.builder()
                .user(user)
                .mockTestId(mockTestId)
                .mockAttempt(attempt)
                .status("IN_PROGRESS")
                .build();

        mockTestAttemptRepository.save(mapping);
    }


    // Update mapping status.
    private void updateMockMapping(Long attemptId, String status) {

        mockTestAttemptRepository.findByMockAttempt_Id(attemptId)
                .ifPresent(mapping -> {

                    mapping.setStatus(status);

                    if ("COMPLETED".equals(status)) {
                        mapping.setCompletedAt(LocalDateTime.now());
                    }

                    mockTestAttemptRepository.save(mapping);
                });
    }
 //===============================================================================================

    @Transactional(readOnly = true)
    public List<MockTestStatusDTO> getMockTestStatuses(Long userId) {

        return mockTestAttemptRepository.findByUser_UserId(userId)
                .stream()
                .map(mapping -> MockTestStatusDTO.builder()
                        .mockTestId(mapping.getMockTestId())
                        .attemptId(mapping.getMockAttempt().getId())
                        .status(mapping.getStatus())
                        .build())
                .toList();
    }
}
