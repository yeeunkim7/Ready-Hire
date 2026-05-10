package com.devinterview.api.domain.payment.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 포트원 결제 단건 조회 응답을 애플리케이션에서 사용하기 위한 DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortOnePaymentResponse {

    private String id;
    private String status;
    private Integer amount;
    private String currency;
    private String orderName;
    /**
     * 포트원 응답의 결제 완료 시각(Unix 초/밀리초 또는 ISO-8601 문자열 등 원문).
     */
    private String paidAt;
}
