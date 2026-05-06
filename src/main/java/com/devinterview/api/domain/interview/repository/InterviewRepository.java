package com.devinterview.api.domain.interview.repository;

import com.devinterview.api.domain.interview.entity.Interview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 搁立 技记 历厘家.
 */
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
