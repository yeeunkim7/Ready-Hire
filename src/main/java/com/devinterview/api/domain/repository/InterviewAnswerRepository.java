package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.InterviewAnswer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    Optional<InterviewAnswer> findByQuestionId(Long questionId);
}
