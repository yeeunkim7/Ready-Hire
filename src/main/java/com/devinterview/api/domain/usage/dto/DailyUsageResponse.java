package com.devinterview.api.domain.usage.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 오늘 면접 사용량 응답 DTO.
 */
@Getter
@Builder
public class DailyUsageResponse {

    private final String planType;
    private final int usedCount;
    private final int dailyLimit;
    private final int remainingCount;
    private final boolean unlimited;
}
