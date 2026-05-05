package com.devinterview.api.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisCommand;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisResult;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;
import com.devinterview.api.domain.ai.service.impl.AiResponseParser;
import com.devinterview.api.domain.ai.service.impl.OpenAiChatService;
import com.devinterview.api.domain.enums.CareerLevel;
import com.devinterview.api.domain.enums.InterviewType;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenAiChatService 주요 성공/파싱 테스트.
 */
class OpenAiChatServiceTest {

    private static final String QUESTIONS_RESPONSE = """
        {
          "model": "gpt-4o-mini",
          "choices": [{
            "message": {
              "content": "1. Java의 GC 동작 원리를 설명해주세요.\\n2. Spring Boot의 자동 설정 원리는?\\n3. JPA N+1 문제와 해결 방법은?\\n4. REST API 설계 원칙을 설명해주세요.\\n5. 스레드 안전성을 보장하는 방법은?"
            }
          }],
          "usage": {
            "prompt_tokens": 111,
            "completion_tokens": 222
          }
        }
        """;

    private static final String ANALYZE_RESPONSE = """
        {
          "model": "gpt-4o-mini",
          "choices": [{
            "message": {
              "content": "{\"score\": 85, \"strengths\": \"핵심 개념을 정확히 이해하고 있음\", \"improvements\": \"실제 사례를 추가하면 좋겠음\", \"modelAnswer\": \"GC는 Young/Old 영역으로 나뉘며...\"}"
            }
          }],
          "usage": {
            "prompt_tokens": 10,
            "completion_tokens": 20
          }
        }
        """;

    private MockWebServer mockWebServer;
    private OpenAiChatService openAiChatService;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ObjectMapper objectMapper = new ObjectMapper();
        AiResponseParser aiResponseParser = new AiResponseParser(objectMapper);

        openAiChatService = new OpenAiChatService(WebClient.builder(), aiResponseParser);
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(openAiChatService, "model", "gpt-4o-mini");
        ReflectionTestUtils.setField(openAiChatService, "baseUrl", mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(openAiChatService, "timeoutSeconds", 2L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("generateQuestions 성공 시 질문 5개를 반환한다")
    void generateQuestions_성공_5개_질문_반환() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(QUESTIONS_RESPONSE));
        QuestionGenerationCommand request = new QuestionGenerationCommand(
            1L,
            InterviewType.TECHNICAL,
            CareerLevel.JUNIOR,
            "Backend Developer",
            "DevView",
            5,
            java.util.List.of("Java", "Spring", "JPA")
        );

        // when
        QuestionGenerationResult result = openAiChatService.generateQuestions(request);

        // then
        assertThat(result.questions()).hasSize(5);
        assertThat(result.questions().get(0)).isEqualTo("Java의 GC 동작 원리를 설명해주세요.");
        assertThat(result.model()).isEqualTo("gpt-4o-mini");
    }

    @Test
    @DisplayName("generateQuestions 빈 응답일 때 예외가 발생한다")
    void generateQuestions_빈_응답_예외_발생() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{" +
            "\"choices\":[{\"message\":{\"content\":\"\"}}]}"));
        QuestionGenerationCommand request = new QuestionGenerationCommand(
            1L,
            InterviewType.TECHNICAL,
            CareerLevel.JUNIOR,
            "Backend Developer",
            "DevView",
            5,
            java.util.List.of("Java")
        );

        // when / then
        assertThatThrownBy(() -> openAiChatService.generateQuestions(request))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("AI");
    }

    @Test
    @DisplayName("analyzeAnswer 성공 시 피드백을 반환한다")
    void analyzeAnswer_성공_피드백_반환() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(ANALYZE_RESPONSE));
        AnswerAnalysisCommand request = new AnswerAnalysisCommand(
            1L,
            10L,
            20L,
            "GC 원리를 설명해주세요",
            "GC는 메모리를 자동으로 정리합니다.",
            "정확성, 깊이, 전달력"
        );

        // when
        AnswerAnalysisResult result = openAiChatService.analyzeAnswer(request);

        // then
        assertThat(result.score()).isEqualTo(85);
        assertThat(result.summaryFeedback()).contains("핵심 개념");
        assertThat(result.detailedFeedbackJson()).contains("improvements");
    }

    @Test
    @DisplayName("analyzeAnswer 파싱 실패 시 예외가 발생한다")
    void analyzeAnswer_파싱_실패_예외_발생() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{" +
            "\"choices\":[{\"message\":{\"content\":\"not-json\"}}]}"));
        AnswerAnalysisCommand request = new AnswerAnalysisCommand(
            1L,
            10L,
            20L,
            "GC 원리를 설명해주세요",
            "GC는 메모리를 자동으로 정리합니다.",
            "정확성, 깊이, 전달력"
        );

        // when / then
        assertThatThrownBy(() -> openAiChatService.analyzeAnswer(request))
            .isInstanceOf(CustomException.class);
    }
}
