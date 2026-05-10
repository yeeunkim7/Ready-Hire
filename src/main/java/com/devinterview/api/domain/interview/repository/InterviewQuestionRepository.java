package com.devinterview.api.domain.interview.repository;

import com.devinterview.api.domain.interview.entity.InterviewQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 면접 질문 저장소.
 */
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findByInterviewIdOrderByQuestionOrder(Long interviewId);
}
