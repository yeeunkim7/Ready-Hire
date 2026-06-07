package com.devinterview.api.domain.interview.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 면접 시작 응답 DTO.
 */
public record InterviewStartResponse(
    Long interviewId,
    List<QuestionDto> questions,
    String sessionMode,
    String interviewMode,
    Integer timeLimitSeconds,
    LocalDateTime questionStartedAt
) {

    public record QuestionDto(
        Long id,
        int order,
        String content
    ) {
    }
}
