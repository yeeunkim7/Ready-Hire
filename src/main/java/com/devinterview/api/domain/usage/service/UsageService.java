package com.devinterview.api.domain.usage.service;

import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.payment.service.SubscriptionPlanSyncService;
import com.devinterview.api.domain.usage.dto.DailyUsageResponse;
import com.devinterview.api.domain.usage.entity.DailyUsage;
import com.devinterview.api.domain.usage.repository.DailyUsageRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일일 면접 사용량 조회 서비스.
 */
@Service
@RequiredArgsConstructor
public class UsageService {

    private static final int FREE_DAILY_LIMIT = 3;

    private final SubscriptionPlanSyncService subscriptionPlanSyncService;
    private final DailyUsageRepository dailyUsageRepository;

    @Transactional
    public DailyUsageResponse getTodayUsage(Long userId) {
        User user = subscriptionPlanSyncService.syncUserPlan(userId);

        if (user.getPlanType() == PlanType.PRO) {
            return DailyUsageResponse.builder()
                .planType(PlanType.PRO.name())
                .usedCount(0)
                .dailyLimit(0)
                .remainingCount(0)
                .unlimited(true)
                .build();
        }

        LocalDate today = LocalDate.now();
        int usedCount = dailyUsageRepository.findByUser_IdAndUsageDate(userId, today)
            .map(DailyUsage::getUsageCount)
            .orElse(0);

        return DailyUsageResponse.builder()
            .planType(PlanType.FREE.name())
            .usedCount(usedCount)
            .dailyLimit(FREE_DAILY_LIMIT)
            .remainingCount(Math.max(0, FREE_DAILY_LIMIT - usedCount))
            .unlimited(false)
            .build();
    }
}
