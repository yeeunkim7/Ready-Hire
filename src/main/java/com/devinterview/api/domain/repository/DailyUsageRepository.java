package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.DailyUsage;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyUsageRepository extends JpaRepository<DailyUsage, Long> {

    Optional<DailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);
}
