package com.devinterview.api.domain.payment.scheduler;

import com.devinterview.api.domain.payment.service.SubscriptionPlanSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 PRO 구독을 주기적으로 FREE로 전환합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final SubscriptionPlanSyncService subscriptionPlanSyncService;

    @Scheduled(cron = "${app.subscription.expiry-cron:0 0 * * * *}")
    public void expireSubscriptions() {
        log.debug("[Subscription] Running scheduled expiry check");
        subscriptionPlanSyncService.expireDueSubscriptions();
    }
}
