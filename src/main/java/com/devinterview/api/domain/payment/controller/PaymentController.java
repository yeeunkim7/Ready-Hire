package com.devinterview.api.domain.payment.controller;

import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.domain.payment.dto.PaymentVerifyRequest;
import com.devinterview.api.domain.payment.dto.PaymentVerifyResponse;
import com.devinterview.api.domain.payment.dto.SubscriptionStatusResponse;
import com.devinterview.api.domain.payment.service.PaymentService;
import com.devinterview.api.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 포트원 결제 검증 및 PRO 구독 관리 REST API 컨트롤러.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> verify(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody PaymentVerifyRequest request
    ) {
        PaymentVerifyResponse data = paymentService.verifyAndActivate(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("결제가 검증되었고 PRO 구독이 활성화되었습니다.", data));
    }

    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> subscription(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SubscriptionStatusResponse data = paymentService.getSubscriptionStatus(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("구독 상태 조회 성공", data));
    }

    @DeleteMapping("/subscription")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        paymentService.cancelSubscription(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("구독이 해지되었습니다.", null));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> webhook(@RequestBody String payload) {
        log.info("[Payment] PortOne webhook received, length={}", payload == null ? 0 : payload.length());
        return ResponseEntity.ok(ApiResponse.success("accepted", null));
    }
}
