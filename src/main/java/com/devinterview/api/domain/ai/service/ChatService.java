package com.devinterview.api.domain.ai.service;

import com.devinterview.api.domain.ai.dto.AnswerAnalysisCommand;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisResult;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;

public interface ChatService {

    QuestionGenerationResult generateQuestions(QuestionGenerationCommand command);

    QuestionGenerationResult generateQuestionsFromPortfolio(
        String jobRole,
        String experienceLevel,
        String portfolioText
    );

    AnswerAnalysisResult analyzeAnswer(AnswerAnalysisCommand command);
}
