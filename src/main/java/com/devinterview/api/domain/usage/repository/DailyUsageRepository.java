package com.devinterview.api.domain.usage.repository;

import com.devinterview.api.domain.usage.entity.DailyUsage;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 老老 荤侩樊 历厘家.
 */
public interface DailyUsageRepository extends JpaRepository<DailyUsage, Long> {

    Optional<DailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate date);
}
