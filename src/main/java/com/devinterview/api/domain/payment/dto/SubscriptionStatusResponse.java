package com.devinterview.api.domain.payment.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 현재 사용자의 플랜·구독·최근 결제 요약 응답 DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {

    private String planType;
    private String subscriptionStatus;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private List<PaymentHistoryDto> recentPayments;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHistoryDto {

        private String portonePaymentId;
        private Integer amount;
        private String status;
        private LocalDateTime paidAt;
    }
}
