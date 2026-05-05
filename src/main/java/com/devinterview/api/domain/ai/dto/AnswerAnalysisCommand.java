package com.devinterview.api.domain.ai.dto;

public record AnswerAnalysisCommand(
    Long userId,
    Long interviewId,
    Long questionId,
    String question,
    String answer,
    String evaluationCriteria
) {
}
