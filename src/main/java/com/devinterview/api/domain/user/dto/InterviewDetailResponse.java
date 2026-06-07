package com.devinterview.api.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewDetailResponse {

    private final Long interviewId;
    private final String jobRole;
    private final List<String> techStack;
    private final String experienceLevel;
    private final String status;
    private final String sessionMode;
    private final String interviewMode;
    private final LocalDateTime createdAt;
    private final int totalScore;
    private final List<QuestionResult> results;

    @Getter
    @Builder
    public static class QuestionResult {
        private final Long questionId;
        private final String questionContent;
        private final Integer score;
        private final String strengths;
        private final String improvements;
        private final String modelAnswer;
    }
}
