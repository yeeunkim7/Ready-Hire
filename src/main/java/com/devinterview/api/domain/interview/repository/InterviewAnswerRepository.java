package com.devinterview.api.domain.interview.repository;

import com.devinterview.api.domain.interview.entity.InterviewAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 면접 답변 저장소.
 */
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    List<InterviewAnswer> findByInterviewId(Long interviewId);
}
