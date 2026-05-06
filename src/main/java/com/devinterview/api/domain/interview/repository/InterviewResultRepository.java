package com.devinterview.api.domain.interview.repository;

import com.devinterview.api.domain.interview.entity.InterviewResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

/**
 * 면접 분석 결과 저장소.
 */
public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {

    @Query("select r from InterviewResult r where r.interview.id = :interviewId order by r.question.id")
    List<InterviewResult> findByInterviewIdOrderByQuestionId(@Param("interviewId") Long interviewId);
}
