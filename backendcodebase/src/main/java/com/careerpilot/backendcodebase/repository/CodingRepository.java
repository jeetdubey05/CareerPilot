package com.careerpilot.backendcodebase.repository;

import com.careerpilot.backendcodebase.entity.CodingQuestion;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodingRepository extends JpaRepository<CodingQuestion,Long> {

}
