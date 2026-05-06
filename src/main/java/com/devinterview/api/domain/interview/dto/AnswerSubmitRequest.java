package com.devinterview.api.domain.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 면접 답변 제출 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class AnswerSubmitRequest {

    @NotNull
    private Long questionId;

    @NotBlank
    private String content;
}
