package com.devinterview.api.domain.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty
    @Size(min = 1, max = 5)
    private List<String> techStack;

    @NotBlank
    private String experienceLevel;
}
