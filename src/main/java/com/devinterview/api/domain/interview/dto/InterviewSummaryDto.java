package com.devinterview.api.domain.interview.dto;

import com.devinterview.api.domain.interview.entity.Interview;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 면접 히스토리 요약 DTO.
 */
@Getter
@Builder
public class InterviewSummaryDto {

    private Long interviewId;
    private String jobRole;
    private List<String> techStack;
    private String experienceLevel;
    private String status;
    private String sessionMode;
    private String interviewMode;
    private LocalDateTime createdAt;

    public static InterviewSummaryDto from(Interview interview) {
        return InterviewSummaryDto.builder()
            .interviewId(interview.getId())
            .jobRole(interview.getJobRole())
            .techStack(interview.getTechStack())
            .experienceLevel(interview.getExperienceLevel())
            .status(interview.getStatus().name())
            .sessionMode(interview.getSessionMode() == null ? null : interview.getSessionMode().name())
            .interviewMode(interview.getInterviewMode())
            .createdAt(interview.getCreatedAt())
            .build();
    }
}
