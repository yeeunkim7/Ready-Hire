package com.devinterview.api.domain.ai.dto;

public record AnswerAnalysisResult(
    int score,
    String summaryFeedback,
    String detailedFeedbackJson,
    String grade,
    String model,
    int promptTokens,
    int completionTokens
) {
}
