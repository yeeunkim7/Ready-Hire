package com.devinterview.api.domain.usage.repository;

import com.devinterview.api.domain.usage.entity.DailyUsage;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyUsageRepository extends JpaRepository<DailyUsage, Long> {

    // \uC0AC\uC6A9\uC790\uC758 \uC624\uB298 \uC0AC\uC6A9\uB7C9 \uC870\uD68C
    Optional<DailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate date);
}
