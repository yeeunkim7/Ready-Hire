package com.devinterview.api.domain.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 면접 답변 제출 응답 DTO.
 */
@Getter
@Builder
@AllArgsConstructor
public class AnswerSubmitResponse {

    private Long answerId;
    private Long resultId;
    private Integer score;
    private String strengths;
    private String improvements;
    private String modelAnswer;
    private boolean isPro;
}
