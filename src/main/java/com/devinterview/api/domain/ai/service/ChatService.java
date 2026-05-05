package com.devinterview.api.domain.ai.service;

import com.devinterview.api.domain.ai.dto.AnswerAnalysisCommand;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisResult;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;

public interface ChatService {

    QuestionGenerationResult generateQuestions(QuestionGenerationCommand command);

    AnswerAnalysisResult analyzeAnswer(AnswerAnalysisCommand command);
}
