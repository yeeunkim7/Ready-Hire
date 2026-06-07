package com.devinterview.api.domain.user.controller;

import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.domain.user.dto.InterviewDetailResponse;
import com.devinterview.api.domain.user.dto.InterviewHistoryPageResponse;
import com.devinterview.api.domain.user.dto.MyPageResponse;
import com.devinterview.api.domain.user.dto.UpdateUserRequest;
import com.devinterview.api.domain.user.dto.WithdrawUserRequest;
import com.devinterview.api.domain.user.service.UserService;
import com.devinterview.api.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyPageResponse data = userService.getMyPage(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("마이페이지 조회 성공", data));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        userService.updateProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("회원 정보가 수정되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody WithdrawUserRequest request
    ) {
        userService.withdraw(userDetails.getUserId(), request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다."));
    }

    @GetMapping("/me/interviews")
    public ResponseEntity<ApiResponse<InterviewHistoryPageResponse>> getMyInterviews(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        InterviewHistoryPageResponse data = userService.getInterviewHistory(userDetails.getUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("면접 히스토리 조회 성공", data));
    }

    @GetMapping("/me/interviews/{interviewId}")
    public ResponseEntity<ApiResponse<InterviewDetailResponse>> getMyInterviewDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long interviewId
    ) {
        InterviewDetailResponse data = userService.getInterviewDetail(userDetails.getUserId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success("면접 상세 조회 성공", data));
    }
}
