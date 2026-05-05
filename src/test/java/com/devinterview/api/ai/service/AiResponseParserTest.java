package com.devinterview.api.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.devinterview.api.domain.ai.service.impl.AiResponseParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AI 응답 파서 순수 단위 테스트.
 */
class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser(new ObjectMapper());

    @Test
    @DisplayName("질문 파싱 시 정상적으로 5개를 추출한다")
    void 질문_파싱_정상_5개_추출() {
        // given
        String content = """
            1. Java의 GC 동작 원리를 설명해주세요.
            2. Spring Boot의 자동 설정 원리는?
            3. JPA N+1 문제와 해결 방법은?
            4. REST API 설계 원칙을 설명해주세요.
            5. 스레드 안전성을 보장하는 방법은?
            """;

        // when
        var result = parser.parseQuestions(content);

        // then
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("질문 파싱 시 번호 매김이 제거된다")
    void 질문_파싱_번호매김_제거_확인() {
        // given
        String content = "1) 첫 질문\n2. 두번째 질문";

        // when
        var result = parser.parseQuestions(content);

        // then
        assertThat(result).containsExactly("첫 질문", "두번째 질문");
    }

    @Test
    @DisplayName("피드백 JSON 파싱이 성공한다")
    void 피드백_JSON_파싱_성공() {
        // given
        String content = """
            {"score":85,"summaryFeedback":"좋음","detailedFeedback":{"strengths":["정확함"]},"grade":"B"}
            """;

        // when
        JsonNode result = parser.parseFeedbackJson(content);

        // then
        assertThat(result.path("score").asInt()).isEqualTo(85);
        assertThat(result.path("grade").asText()).isEqualTo("B");
    }

    @Test
    @DisplayName("피드백 JSON 필드 누락 시 기본값 처리 가능하다")
    void 피드백_JSON_필드_누락_기본값_처리() {
        // given
        String content = "{}";

        // when
        JsonNode result = parser.parseFeedbackJson(content);

        // then
        assertThat(result.path("score").asInt(0)).isEqualTo(0);
        assertThat(result.path("summaryFeedback").asText("")).isEmpty();
    }
}
