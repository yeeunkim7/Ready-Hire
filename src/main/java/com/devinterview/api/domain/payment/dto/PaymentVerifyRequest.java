package com.devinterview.api.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 프론트에서 전달한 포트원 결제 식별자 및 주문 정보 검증 요청 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
public class PaymentVerifyRequest {

    @NotBlank
    private String paymentId;

    @NotBlank
    private String orderName;

    @NotNull
    private Integer amount;
}
