package com.devinterview.api.domain.interview.repository;

import com.devinterview.api.domain.interview.entity.Interview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // \uC0AC\uC6A9\uC790\uBCC4 \uBA74\uC811 \uBAA9\uB85D \uCD5C\uC2E0\uC21C \uC870\uD68C
    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
