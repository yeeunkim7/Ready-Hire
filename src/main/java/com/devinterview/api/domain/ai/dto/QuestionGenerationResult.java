package com.devinterview.api.domain.ai.dto;

import java.util.List;

public record QuestionGenerationResult(
    List<String> questions,
    String model,
    int promptTokens,
    int completionTokens
) {
}
