package com.careerpilot.backendcodebase.repository;

import com.careerpilot.backendcodebase.dtos.QuestionResponseDTO;
import com.careerpilot.backendcodebase.entity.DifficultyLevel;
import com.careerpilot.backendcodebase.entity.Question;
import com.careerpilot.backendcodebase.entity.QuestionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    List<Question> findByCategoryAndDifficulty(QuestionCategory category, DifficultyLevel difficulty);
    List<Question> findByCategory(QuestionCategory category);

    List<Question> findByDifficulty(DifficultyLevel difficulty);
    @Query(value = """
            SELECT *
            FROM question
            ORDER BY RAND()
            LIMIT :count
            """, nativeQuery = true)
    List<Question> findRandomQuestions(@Param("count") int count);


    @Query("""
select q from Question q JOIN
  q.company c WHERE c.companyId=:companyId AND
  (:category is NULL OR q.category=:category) AND
  (:difficulty is NULL OR q.difficulty=:difficulty)
""")
    Page<Question> findByCompany(@Param("companyId") Long companyId,
                                            @Param("difficulty") DifficultyLevel difficulty,
                                            @Param("category") QuestionCategory category, Pageable pageable);


@Query(value = """
        SELECT *
        FROM question
        WHERE category IN (:categories)
        ORDER BY RAND()
        LIMIT :count
        """, nativeQuery = true)
List<Question> findRandomQuestionsByCategories(
        @Param("categories") List<String> categories,
        @Param("count") int count
);
}
