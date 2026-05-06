package com.devinterview.api.domain.ai.dto;

import com.devinterview.api.domain.enums.CareerLevel;
import com.devinterview.api.domain.enums.InterviewType;
import java.util.List;

public record QuestionGenerationCommand(
    Long userId,
    InterviewType interviewType,
    CareerLevel careerLevel,
    String jobPosition,
    String companyName,
    int questionCount,
    List<String> focusTopics
) {
}
