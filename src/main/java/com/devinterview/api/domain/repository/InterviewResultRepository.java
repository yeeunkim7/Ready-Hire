package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.InterviewResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {

    Optional<InterviewResult> findByInterviewId(Long interviewId);
}
