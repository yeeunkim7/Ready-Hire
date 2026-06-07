package com.devinterview.api.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewHistoryResponse {

    private final Long interviewId;
    private final String jobRole;
    private final List<String> techStack;
    private final String experienceLevel;
    private final String status;
    private final LocalDateTime createdAt;
    private final Integer totalScore;
}
