
package com.careerpilot.backendcodebase.service;

import com.careerpilot.backendcodebase.entity.Question;
import com.careerpilot.backendcodebase.entity.QuestionCategory;
import com.careerpilot.backendcodebase.repository.CompanyQuestionRepository;
import com.careerpilot.backendcodebase.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionSelectionService {

    private final QuestionRepository questionRepository;
    private final CompanyQuestionRepository companyQuestionRepository;

    // Constructor Injection
    public QuestionSelectionService(
            QuestionRepository questionRepository,
            CompanyQuestionRepository companyQuestionRepository) {

        this.questionRepository = questionRepository;
        this.companyQuestionRepository = companyQuestionRepository;
    }

    // 1. Select random questions from all categories
    public List<Question> selectQuestions(int numberOfQuestions) {

        return questionRepository.findRandomQuestions(numberOfQuestions);
    }

    // 2. Select random questions from specified categories
    public List<Question> selectQuestions(
            int numberOfQuestions,
            List<QuestionCategory> categories) {

        if (categories == null || categories.isEmpty()) {
            return selectQuestions(numberOfQuestions);
        }

        List<String> categoryNames = categories.stream()
                .map(Enum::name)
                .toList();

        return questionRepository.findRandomQuestionsByCategories(
                categoryNames,
                numberOfQuestions
        );
    }

    // 3. Select company-specific PYQ questions
    public List<Question> selectCompanyQuestions(
            Long companyId,
            int numberOfQuestions) {

        List<Question> questions =
                companyQuestionRepository.findQuestionsByCompanyId(companyId);

        Collections.shuffle(questions);

        return questions.stream()
                .limit(numberOfQuestions)
                .toList();
    }
}
