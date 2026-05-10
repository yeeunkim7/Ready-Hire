package com.devinterview.api.domain.payment.repository;

import com.devinterview.api.domain.enums.SubscriptionStatus;
import com.devinterview.api.domain.payment.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 구독 정보 저장소.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUser_IdAndStatus(Long userId, SubscriptionStatus status);
}
