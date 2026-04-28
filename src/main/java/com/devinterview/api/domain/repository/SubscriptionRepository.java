package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.Subscription;
import com.devinterview.api.domain.enums.SubscriptionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
