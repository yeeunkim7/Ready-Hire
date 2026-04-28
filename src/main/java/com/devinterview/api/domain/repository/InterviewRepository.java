package com.devinterview.api.domain.repository;

import com.devinterview.api.domain.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
}
