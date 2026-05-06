package com.devinterview.api.domain.interview.controller;

import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.domain.interview.dto.AnswerSubmitRequest;
import com.devinterview.api.domain.interview.dto.AnswerSubmitResponse;
import com.devinterview.api.domain.interview.dto.InterviewCompleteResponse;
import com.devinterview.api.domain.interview.dto.InterviewStartRequest;
import com.devinterview.api.domain.interview.dto.InterviewStartResponse;
import com.devinterview.api.domain.interview.dto.InterviewSummaryDto;
import com.devinterview.api.domain.interview.service.InterviewService;
import com.devinterview.api.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 세션 시작 및 히스토리 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewStartResponse>> startInterview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody InterviewStartRequest request
    ) {
        InterviewStartResponse response = interviewService.startInterview(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("면접 세션이 시작되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewSummaryDto>>> getHistory(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<InterviewSummaryDto> history = interviewService.getHistory(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("면접 히스토리 조회 성공", history));
    }

    @PostMapping("/{interviewId}/answers")
    public ResponseEntity<ApiResponse<AnswerSubmitResponse>> submitAnswer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long interviewId,
        @Valid @RequestBody AnswerSubmitRequest request
    ) {
        AnswerSubmitResponse response = interviewService.submitAnswer(userDetails.getUserId(), interviewId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("답변이 제출되고 피드백이 생성되었습니다.", response));
    }

    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<ApiResponse<InterviewCompleteResponse>> completeInterview(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long interviewId
    ) {
        InterviewCompleteResponse response = interviewService.completeInterview(userDetails.getUserId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success("면접이 종료되었습니다.", response));
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<InterviewCompleteResponse>> getInterviewDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long interviewId
    ) {
        InterviewCompleteResponse response = interviewService.getInterviewDetail(userDetails.getUserId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success("면접 상세 조회 성공", response));
    }
}
