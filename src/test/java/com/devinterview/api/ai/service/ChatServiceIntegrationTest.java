package com.devinterview.api.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.devinterview.api.domain.ai.service.ChatService;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

/**
 * ChatService 스프링 통합 로딩 테스트.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "openai.api.key=test-api-key",
    "openai.model=gpt-4o-mini"
})
class ChatServiceIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private ChatService chatService;

    @BeforeAll
    static void beforeAll() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws Exception {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("openai.base-url", () -> mockWebServer.url("/").toString());
        registry.add("openai.timeout-seconds", () -> 1);
    }

    @Test
    @DisplayName("Spring Context에서 ChatService 빈이 정상 주입된다")
    void spring_context_로드_및_chatService_빈_주입_확인() {
        // given / when / then
        assertThat(chatService).isNotNull();
        assertThat(chatService.getClass().getSimpleName()).isEqualTo("OpenAiChatService");
    }
}
