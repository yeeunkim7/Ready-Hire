package com.devinterview.api.domain.payment.service;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.enums.SubscriptionStatus;
import com.devinterview.api.domain.payment.entity.Subscription;
import com.devinterview.api.domain.payment.repository.SubscriptionRepository;
import com.devinterview.api.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 만료 처리 및 users.plan_type 동기화.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPlanSyncService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void expireDueSubscriptions() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<Subscription> due = subscriptionRepository.findByStatusAndExpiresAtBefore(SubscriptionStatus.ACTIVE, now);

        for (Subscription subscription : due) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            User user = subscription.getUser();
            if (user.getPlanType() == PlanType.PRO) {
                user.setPlanType(PlanType.FREE);
                log.info("[Subscription] Expired and downgraded: userId={}, subscriptionId={}",
                    user.getId(), subscription.getId());
            }
        }
    }

    @Transactional
    public User syncUserPlan(Long userId) {
        expireDueSubscriptions();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Subscription active = subscriptionRepository.findByUser_IdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElse(null);

        if (active == null) {
            if (user.getPlanType() == PlanType.PRO) {
                user.setPlanType(PlanType.FREE);
                log.info("[Subscription] No active subscription; downgraded userId={}", userId);
            }
            return user;
        }

        if (active.getExpiresAt() != null && active.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            active.setStatus(SubscriptionStatus.EXPIRED);
            user.setPlanType(PlanType.FREE);
            log.info("[Subscription] Active subscription past expiry; downgraded userId={}, subscriptionId={}",
                userId, active.getId());
            return user;
        }

        if (user.getPlanType() != PlanType.PRO) {
            user.setPlanType(PlanType.PRO);
            log.info("[Subscription] Restored PRO plan from active subscription: userId={}", userId);
        }

        return user;
    }
}
