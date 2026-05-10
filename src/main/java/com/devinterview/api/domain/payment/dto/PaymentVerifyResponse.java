package com.devinterview.api.domain.payment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 검증 및 PRO 플랜 활성화 결과 DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyResponse {

    private String portonePaymentId;
    private Integer amount;
    private String status;
    private String planType;
    private LocalDateTime paidAt;
    private LocalDateTime subscriptionExpiresAt;
}
