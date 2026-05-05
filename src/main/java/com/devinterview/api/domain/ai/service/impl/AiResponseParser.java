package com.devinterview.api.domain.ai.service.impl;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public List<String> parseQuestions(String content) {
        JsonNode parsed = tryParseJson(content);
        if (parsed != null && parsed.has("questions") && parsed.get("questions").isArray()) {
            List<String> questions = new ArrayList<>();
            for (JsonNode q : parsed.get("questions")) {
                String value = sanitizeQuestionLine(q.asText());
                if (!value.isBlank()) {
                    questions.add(value);
                }
            }
            return questions;
        }

        List<String> questions = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String value = sanitizeQuestionLine(line);
            if (!value.isBlank()) {
                questions.add(value);
            }
        }
        return questions;
    }

    public JsonNode parseFeedbackJson(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException ex) {
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, "OpenAI response is not valid JSON.");
        }
    }

    private JsonNode tryParseJson(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String sanitizeQuestionLine(String line) {
        return line
            .replaceFirst("^\\s*\\d+[.)]\\s*", "")
            .replaceFirst("^\\s*[-*]\\s*", "")
            .trim();
    }
}
