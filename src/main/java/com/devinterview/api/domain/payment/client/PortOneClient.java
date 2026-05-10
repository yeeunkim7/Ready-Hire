package com.devinterview.api.domain.payment.client;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * 포트원 V2 결제 단건 조회 REST 클라이언트.
 */
@Slf4j
@Component
public class PortOneClient {

    private final WebClient portOneWebClient;

    private final ObjectMapper objectMapper;

    public PortOneClient(@Qualifier("portOneWebClient") WebClient portOneWebClient, ObjectMapper objectMapper) {
        this.portOneWebClient = portOneWebClient;
        this.objectMapper = objectMapper;
    }

    @Value("${portone.v2.api-secret:}")
    private String apiSecret;

    @Value("${portone.v2.timeout-seconds:10}")
    private long timeoutSeconds;

    public PortOnePaymentResponse getPayment(String paymentId) {
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new CustomException(ErrorCode.PAYMENT_PORTONE_API_ERROR, "PortOne API secret is not configured.");
        }
        log.info("[PortOne] Request payment inquiry: paymentId={}", paymentId);
        try {
            String raw = portOneWebClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret.strip())
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(Math.max(timeoutSeconds, 1)));

            if (raw == null || raw.isBlank()) {
                throw new CustomException(ErrorCode.PAYMENT_PORTONE_API_ERROR, "Empty response from PortOne.");
            }

            JsonNode root = objectMapper.readTree(raw);
            JsonNode paymentNode = unwrapPayment(root);

            PortOnePaymentResponse response = parsePaymentNode(paymentNode);
            log.info("[PortOne] Payment inquiry succeeded: paymentId={}, status={}, amount={}", response.getId(), response.getStatus(), response.getAmount());
            return response;
        } catch (CustomException ex) {
            throw ex;
        } catch (WebClientResponseException.NotFound ex) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
        } catch (WebClientResponseException ex) {
            log.warn("[PortOne] HTTP error: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new CustomException(ErrorCode.PAYMENT_PORTONE_API_ERROR, ex.getMessage());
        } catch (Exception ex) {
            log.warn("[PortOne] Call failed: {}", ex.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_PORTONE_API_ERROR, ex.getMessage());
        }
    }

    private static JsonNode unwrapPayment(JsonNode root) {
        if (root == null || root.isNull()) {
            return root;
        }
        if (root.hasNonNull("payment")) {
            return root.get("payment");
        }
        return root;
    }

    private PortOnePaymentResponse parsePaymentNode(JsonNode payment) {
        if (payment == null || payment.isNull() || payment.isMissingNode()) {
            throw new CustomException(ErrorCode.PAYMENT_PORTONE_API_ERROR, "Payment payload is missing.");
        }

        String id = firstNonBlank(text(payment, "id"), text(payment, "paymentId"));
        String status = text(payment, "status");
        Integer amount = extractAmount(payment);
        String currency = text(payment, "currency");
        String orderName = firstNonBlank(text(payment, "orderName"), text(payment, "name"));
        String paidAtRaw = extractPaidAtRaw(payment);

        return PortOnePaymentResponse.builder()
            .id(id)
            .status(status)
            .amount(amount)
            .currency(currency)
            .orderName(orderName)
            .paidAt(paidAtRaw)
            .build();
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return s == null || s.isBlank() ? null : s;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static Integer extractAmount(JsonNode payment) {
        if (payment.hasNonNull("amountTotal") && payment.get("amountTotal").canConvertToInt()) {
            return payment.get("amountTotal").asInt();
        }
        JsonNode amount = payment.path("amount");
        if (amount.isObject() && amount.path("total").canConvertToInt()) {
            return amount.path("total").asInt();
        }
        if (amount.isIntegralNumber()) {
            return amount.asInt();
        }
        if (payment.path("totalAmount").canConvertToInt()) {
            return payment.path("totalAmount").asInt();
        }
        return null;
    }

    private static String extractPaidAtRaw(JsonNode payment) {
        JsonNode paidAt = payment.get("paidAt");
        if (paidAt == null || paidAt.isNull() || paidAt.isMissingNode()) {
            return null;
        }
        if (paidAt.isNumber()) {
            return paidAt.asText();
        }
        if (paidAt.isTextual()) {
            return paidAt.asText();
        }
        if (paidAt.isObject()) {
            return paidAt.toString();
        }
        return null;
    }
}
