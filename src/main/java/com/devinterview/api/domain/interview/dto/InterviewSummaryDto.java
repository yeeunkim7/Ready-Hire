package com.devinterview.api.domain.interview.dto;

import java.time.OffsetDateTime;

/**
 * 면접 히스토리 요약 DTO.
 */
public record InterviewSummaryDto(
    Long id,
    String jobRole,
    String status,
    OffsetDateTime createdAt
) {
}
