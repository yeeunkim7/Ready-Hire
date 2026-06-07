package com.devinterview.api.domain.interview.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 면접 종료/상세 조회 응답 DTO.
 */
@Getter
@Builder
@AllArgsConstructor
public class InterviewCompleteResponse {

    private Long interviewId;
    private String status;
    private String sessionMode;
    private String interviewMode;
    private int totalScore;
    private List<ResultSummary> results;
    private List<InterviewStartResponse.QuestionDto> questions;
    private Integer answeredCount;
    private Integer timeLimitSeconds;
    private LocalDateTime questionStartedAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ResultSummary {
        private Long questionId;
        private String questionContent;
        private Integer score;
        private String strengths;
        private String improvements;
        private String modelAnswer;
    }
}
