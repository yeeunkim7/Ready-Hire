package com.devinterview.api.domain.usage.controller;

import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.domain.usage.dto.DailyUsageResponse;
import com.devinterview.api.domain.usage.service.UsageService;
import com.devinterview.api.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용량 조회 API.
 */
@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DailyUsageResponse>> getTodayUsage(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        DailyUsageResponse data = usageService.getTodayUsage(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("오늘 사용량 조회 성공", data));
    }
}
