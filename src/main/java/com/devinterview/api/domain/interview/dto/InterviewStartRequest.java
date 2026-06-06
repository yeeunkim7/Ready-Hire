package com.devinterview.api.domain.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 면접 시작 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class InterviewStartRequest {

    @NotBlank
    private String jobRole;

    @Size(max = 5)
    private List<String> techStack;

    @NotBlank
    private String experienceLevel;

    /** PDF에서 추출한 채용공고 텍스트 (JOB_POSTING 모드) */
    private String jobPostingText;

    /** STANDARD (기본) / JOB_POSTING (채용공고 기반) */
    private String interviewMode;
}
