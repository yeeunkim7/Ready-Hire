package com.devinterview.api.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.service.impl.AiResponseParser;
import com.devinterview.api.domain.ai.service.impl.OpenAiChatService;
import com.devinterview.api.domain.enums.CareerLevel;
import com.devinterview.api.domain.enums.InterviewType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OpenAI 오류 응답 및 네트워크 장애 테스트.
 */
class OpenAiChatServiceErrorTest {

    private MockWebServer mockWebServer;
    private OpenAiChatService openAiChatService;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        openAiChatService = new OpenAiChatService(WebClient.builder(), new AiResponseParser(new ObjectMapper()));
        ReflectionTestUtils.setField(openAiChatService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(openAiChatService, "model", "gpt-4o-mini");
        ReflectionTestUtils.setField(openAiChatService, "baseUrl", mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(openAiChatService, "timeoutSeconds", 1L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("OpenAI 500 에러 시 AI_SERVICE_ERROR 예외를 던진다")
    void openAI_500에러_AI_SERVICE_ERROR_예외() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("internal-error"));

        // when / then
        assertThatThrownBy(() -> openAiChatService.generateQuestions(command()))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode()).isEqualTo(ErrorCode.AI_SERVICE_ERROR));
    }

    @Test
    @DisplayName("OpenAI 401 에러 시 인증 실패 예외를 던진다")
    void openAI_401에러_인증실패_예외() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(401).setBody("unauthorized"));

        // when / then
        assertThatThrownBy(() -> openAiChatService.generateQuestions(command()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("OpenAI API call failed");
    }

    @Test
    @DisplayName("OpenAI 타임아웃 상황에서 예외를 던진다")
    void openAI_타임아웃_예외() {
        // given
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        // when / then
        assertThatThrownBy(() -> openAiChatService.generateQuestions(command()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("OpenAI API call failed");
    }

    @Test
    @DisplayName("OpenAI choices 배열이 비어 있으면 예외를 던진다")
    void openAI_빈_choices_배열_예외() {
        // given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"choices\":[]}"));

        // when / then
        assertThatThrownBy(() -> openAiChatService.generateQuestions(command()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining("empty completion response");
    }

    private QuestionGenerationCommand command() {
        return new QuestionGenerationCommand(
            1L,
            InterviewType.TECHNICAL,
            CareerLevel.JUNIOR,
            "Backend Developer",
            "DevView",
            5,
            List.of("Java", "Spring")
        );
    }
}
