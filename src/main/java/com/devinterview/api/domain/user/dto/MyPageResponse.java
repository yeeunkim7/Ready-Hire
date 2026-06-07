package com.devinterview.api.domain.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageResponse {

    private final String email;
    private final String planType;
    private final String provider;
    private final LocalDateTime joinedAt;
    private final List<InterviewHistoryResponse> recentInterviews;
}
