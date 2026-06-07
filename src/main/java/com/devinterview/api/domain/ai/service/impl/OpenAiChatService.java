package com.devinterview.api.domain.ai.service.impl;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisCommand;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisResult;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;
import com.devinterview.api.domain.ai.service.ChatService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiChatService implements ChatService {

    private static final String MODE_JOB_POSTING = "JOB_POSTING";

    private static final String OPENAI_CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private static final String QUESTION_SYSTEM_MESSAGE = """
        You are an expert technical interviewer.
        Generate interview questions in Korean based on candidate profile.
        Return JSON only with this schema:
        {
          \"questions\": [\"question 1\", \"question 2\", ...]
        }
        """;

    private static final String ANALYSIS_SYSTEM_MESSAGE = """
        You are an expert interview evaluator.
        Evaluate the candidate answer fairly and provide concise actionable feedback.
        Return JSON only with this schema:
        {
          \"score\": 0,
          \"summaryFeedback\": \"...\",
          \"detailedFeedback\": {
            \"strengths\": [\"...\"],
            \"improvements\": [\"...\"],
            \"nextStep\": \"...\"
          },
          \"grade\": \"A\"
        }
        """;

    private final WebClient.Builder webClientBuilder;
    private final AiResponseParser aiResponseParser;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.timeout-seconds:5}")
    private long timeoutSeconds;

    @Override
    public QuestionGenerationResult generateQuestions(QuestionGenerationCommand command) {
        log.info("[AI] Question generation mode={}", command.interviewMode());
        String userPrompt = buildQuestionPrompt(command);
        ChatCompletionResponse response = requestChatCompletion(QUESTION_SYSTEM_MESSAGE, userPrompt);

        String content = extractContent(response);
        List<String> questions = aiResponseParser.parseQuestions(content);

        if (questions.isEmpty()) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI question response parsing failed.");
        }

        return new QuestionGenerationResult(
            questions,
            response.model(),
            response.usage() == null ? 0 : response.usage().promptTokens(),
            response.usage() == null ? 0 : response.usage().completionTokens()
        );
    }

    @Override
    public QuestionGenerationResult generateQuestionsFromPortfolio(
        String jobRole,
        String experienceLevel,
        String portfolioText
    ) {
        log.info("[AI] PORTFOLIO question generation started: jobRole={}, textLength={}",
            jobRole, portfolioText == null ? 0 : portfolioText.length());

        String userPrompt = buildPortfolioQuestionPrompt(jobRole, experienceLevel, portfolioText);
        ChatCompletionResponse response = requestChatCompletion(QUESTION_SYSTEM_MESSAGE, userPrompt);

        String content = extractContent(response);
        List<String> questions = aiResponseParser.parseQuestions(content);

        if (questions.isEmpty()) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI portfolio question response parsing failed.");
        }

        log.info("[AI] PORTFOLIO question generation completed: questionCount={}", questions.size());

        return new QuestionGenerationResult(
            questions,
            response.model(),
            response.usage() == null ? 0 : response.usage().promptTokens(),
            response.usage() == null ? 0 : response.usage().completionTokens()
        );
    }

    @Override
    public AnswerAnalysisResult analyzeAnswer(AnswerAnalysisCommand command) {
        String userPrompt = buildAnswerAnalysisPrompt(command);
        ChatCompletionResponse response = requestChatCompletion(ANALYSIS_SYSTEM_MESSAGE, userPrompt);

        String content = extractContent(response);
        JsonNode root = aiResponseParser.parseFeedbackJson(content);

        int score = root.path("score").asInt(0);
        String summaryFeedback = root.path("summaryFeedback").asText(
            root.path("strengths").asText("")
        );
        JsonNode detailedFeedback = root.path("detailedFeedback");
        if (detailedFeedback.isMissingNode()) {
            detailedFeedback = root;
        }
        String detailedFeedbackJson = detailedFeedback.isMissingNode() ? "{}" : detailedFeedback.toString();
        String grade = root.path("grade").asText("C");

        return new AnswerAnalysisResult(
            score,
            summaryFeedback,
            detailedFeedbackJson,
            grade,
            response.model(),
            response.usage() == null ? 0 : response.usage().promptTokens(),
            response.usage() == null ? 0 : response.usage().completionTokens()
        );
    }

    private ChatCompletionResponse requestChatCompletion(String systemMessage, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OPENAI_API_KEY is not configured.");
        }

        ChatCompletionRequest request = new ChatCompletionRequest(
            model,
            List.of(
                new ChatMessage("system", systemMessage),
                new ChatMessage("user", userMessage)
            ),
            0.3
        );

        try {
            ChatCompletionResponse response = webClientBuilder
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri(OPENAI_CHAT_COMPLETIONS_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block(Duration.ofSeconds(timeoutSeconds));

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI returned empty completion response.");
            }

            return response;
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI API call failed: " + ex.getMessage());
        }
    }

    private String extractContent(ChatCompletionResponse response) {
        Choice firstChoice = response.choices().get(0);
        if (firstChoice == null || firstChoice.message() == null || firstChoice.message().content() == null) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI response content is missing.");
        }
        return firstChoice.message().content();
    }

    private String sanitizeFormatArg(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("%", "%%");
    }

    private String buildPortfolioQuestionPrompt(String jobRole, String experienceLevel, String portfolioText) {
        return """
            당신은 경험 많은 면접관입니다.
            아래는 지원자의 포트폴리오/이력서 내용입니다:
            %s

            지원 직무: %s
            경력: %s

            위 포트폴리오를 바탕으로 지원자의 실제 경험과 역량을
            깊이 파악할 수 있는 면접 질문 5개를 생성하세요.

            질문 유형:
            - 프로젝트 경험 구체화 (STAR 기법 유도)
            - 기술적 의사결정 이유
            - 어려움 극복 경험
            - 성과와 배운 점
            - 향후 성장 방향

            질문은 한국어로 작성하고 JSON 스키마에 맞게 반환하세요.
            """.formatted(sanitizeFormatArg(portfolioText), sanitizeFormatArg(jobRole), sanitizeFormatArg(experienceLevel));
    }

    private String buildQuestionPrompt(QuestionGenerationCommand command) {
        if (MODE_JOB_POSTING.equalsIgnoreCase(command.interviewMode())) {
            return """
                당신은 전문 면접관입니다.
                아래는 채용공고 내용입니다:
                %s

                이 채용공고에 지원하는 %s 지원자를 위한
                핵심 역량 중심 면접 질문 %d개를 생성하세요.
                직무: %s

                질문은 한국어로 작성하고 JSON 스키마에 맞게 반환하세요.
                """.formatted(
                sanitizeFormatArg(command.jobPostingText()),
                sanitizeFormatArg(command.companyName() != null ? command.companyName() : command.careerLevel().name()),
                command.questionCount(),
                sanitizeFormatArg(command.jobPosition())
            );
        }

        String topics = command.focusTopics() == null || command.focusTopics().isEmpty()
            ? "(없음)"
            : String.join(", ", command.focusTopics());

        return """
            당신은 전문 면접관입니다.
            직무: %s, 기술스택: %s, 경력: %s
            위 조건에 맞는 면접 질문 %d개를 생성하세요.

            질문은 한국어로 작성하고 JSON 스키마에 맞게 반환하세요.
            """.formatted(
            sanitizeFormatArg(command.jobPosition()),
            sanitizeFormatArg(topics),
            sanitizeFormatArg(command.companyName() != null ? command.companyName() : command.careerLevel().name()),
            command.questionCount()
        );
    }

    private String buildAnswerAnalysisPrompt(AnswerAnalysisCommand command) {
        return """
            Evaluate this interview answer.

            Context:
            - User ID: %d
            - Interview ID: %d
            - Question ID: %d
            - Question: %s
            - Candidate Answer: %s
            - Evaluation Criteria: %s

            Score should be from 0 to 100.
            Grade must be one of A, B, C, D, F.
            """.formatted(
            command.userId(),
            command.interviewId(),
            command.questionId(),
            command.question(),
            command.answer(),
            command.evaluationCriteria() == null ? "General quality, clarity, and technical depth" : command.evaluationCriteria()
        );
    }

    private record ChatCompletionRequest(
        String model,
        List<ChatMessage> messages,
        double temperature
    ) {
    }

    private record ChatMessage(
        String role,
        String content
    ) {
    }

    private record ChatCompletionResponse(
        String model,
        List<Choice> choices,
        Usage usage
    ) {
    }

    private record Choice(
        int index,
        ChatMessage message
    ) {
    }

    private record Usage(
        @JsonProperty("prompt_tokens") int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens
    ) {
    }
}
