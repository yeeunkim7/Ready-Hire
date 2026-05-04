package com.devinterview.api.domain.ai.service.impl;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisCommand;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisResult;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;
import com.devinterview.api.domain.ai.service.ChatService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class OpenAiChatService implements ChatService {

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
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Override
    public QuestionGenerationResult generateQuestions(QuestionGenerationCommand command) {
        String userPrompt = buildQuestionPrompt(command);
        ChatCompletionResponse response = requestChatCompletion(QUESTION_SYSTEM_MESSAGE, userPrompt);

        String content = extractContent(response);
        JsonNode root = parseJson(content);

        List<String> questions = new ArrayList<>();
        JsonNode questionNode = root.get("questions");
        if (questionNode != null && questionNode.isArray()) {
            for (JsonNode node : questionNode) {
                questions.add(node.asText());
            }
        }

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
    public AnswerAnalysisResult analyzeAnswer(AnswerAnalysisCommand command) {
        String userPrompt = buildAnswerAnalysisPrompt(command);
        ChatCompletionResponse response = requestChatCompletion(ANALYSIS_SYSTEM_MESSAGE, userPrompt);

        String content = extractContent(response);
        JsonNode root = parseJson(content);

        int score = root.path("score").asInt(0);
        String summaryFeedback = root.path("summaryFeedback").asText("");
        JsonNode detailedFeedback = root.path("detailedFeedback");
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
                .baseUrl("https://api.openai.com")
                .build()
                .post()
                .uri(OPENAI_CHAT_COMPLETIONS_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .block();

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

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI response is not valid JSON.");
        }
    }

    private String buildQuestionPrompt(QuestionGenerationCommand command) {
        String topics = command.focusTopics() == null || command.focusTopics().isEmpty()
            ? "(none)"
            : String.join(", ", command.focusTopics());

        return """
            Candidate Profile:
            - User ID: %d
            - Interview Type: %s
            - Career Level: %s
            - Job Position: %s
            - Target Company: %s
            - Focus Topics(Tech Stack): %s
            - Question Count: %d

            Generate practical interview questions appropriate for this candidate.
            """.formatted(
            command.userId(),
            command.interviewType(),
            command.careerLevel(),
            command.jobPosition(),
            command.companyName() == null ? "N/A" : command.companyName(),
            topics,
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
