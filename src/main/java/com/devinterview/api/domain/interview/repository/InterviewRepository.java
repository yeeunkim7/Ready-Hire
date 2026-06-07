package com.devinterview.api.domain.interview.repository;

import com.devinterview.api.domain.interview.entity.Interview;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Interview> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);

    Page<Interview> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
