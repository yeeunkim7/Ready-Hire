package com.devinterview.api.domain.payment.service;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.PaymentStatus;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.enums.SubscriptionPlanType;
import com.devinterview.api.domain.enums.SubscriptionStatus;
import com.devinterview.api.domain.payment.client.PortOneClient;
import com.devinterview.api.domain.payment.client.PortOnePaymentResponse;
import com.devinterview.api.domain.payment.dto.PaymentVerifyRequest;
import com.devinterview.api.domain.payment.dto.PaymentVerifyResponse;
import com.devinterview.api.domain.payment.dto.SubscriptionStatusResponse;
import com.devinterview.api.domain.payment.entity.Payment;
import com.devinterview.api.domain.payment.entity.Subscription;
import com.devinterview.api.domain.payment.repository.PaymentRepository;
import com.devinterview.api.domain.payment.repository.SubscriptionRepository;
import com.devinterview.api.domain.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포트원 결제 검증, 구독 생성/해지, 사용자 플랜(PRO) 반영을 담당하는 서비스.
 */
@Slf4j
@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PortOneClient portOneClient;
    private final SubscriptionPlanSyncService subscriptionPlanSyncService;

    private final int subscriptionPrice;
    private final int subscriptionDurationDays;

    public PaymentService(
        UserRepository userRepository,
        SubscriptionRepository subscriptionRepository,
        PaymentRepository paymentRepository,
        PortOneClient portOneClient,
        SubscriptionPlanSyncService subscriptionPlanSyncService,
        @Value("${app.subscription.price}") int subscriptionPrice,
        @Value("${app.subscription.duration-days:30}") int subscriptionDurationDays
    ) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.portOneClient = portOneClient;
        this.subscriptionPlanSyncService = subscriptionPlanSyncService;
        this.subscriptionPrice = subscriptionPrice;
        this.subscriptionDurationDays = subscriptionDurationDays;
    }

    @Transactional
    public PaymentVerifyResponse verifyAndActivate(Long userId, PaymentVerifyRequest request) {
        log.info("[Payment] Verify start: userId={}, paymentId={}", userId, request.getPaymentId());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_ERROR, "사용자를 찾을 수 없습니다."));

        if (paymentRepository.findByPortonePaymentId(request.getPaymentId()).isPresent()) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        if (!Integer.valueOf(subscriptionPrice).equals(request.getAmount())) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PortOnePaymentResponse remote = portOneClient.getPayment(request.getPaymentId());
        validatePortOneResponse(request, remote);

        expireActiveSubscriptions(userId);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expires = now.plusDays(subscriptionDurationDays);

        Subscription subscription = subscriptionRepository.save(
            Subscription.builder()
                .user(user)
                .planType(SubscriptionPlanType.PRO)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(now)
                .expiresAt(expires)
                .build()
        );

        LocalDateTime paidAt = parsePaidAt(remote.getPaidAt(), now);

        Payment payment = paymentRepository.save(
            Payment.builder()
                .user(user)
                .subscription(subscription)
                .portonePaymentId(remote.getId())
                .amount(remote.getAmount() != null ? remote.getAmount() : subscriptionPrice)
                .currency(remote.getCurrency() != null ? remote.getCurrency() : "KRW")
                .status(PaymentStatus.PAID)
                .paidAt(paidAt)
                .build()
        );

        user.setPlanType(PlanType.PRO);
        userRepository.save(user);

        log.info("[Payment] PRO activated: userId={}, subscriptionId={}, paymentDbId={}, portonePaymentId={}",
            userId, subscription.getId(), payment.getId(), remote.getId());

        return PaymentVerifyResponse.builder()
            .portonePaymentId(remote.getId())
            .amount(remote.getAmount())
            .status(PaymentStatus.PAID.name())
            .planType(PlanType.PRO.name())
            .paidAt(paidAt)
            .subscriptionExpiresAt(expires)
            .build();
    }

    @Transactional
    public SubscriptionStatusResponse getSubscriptionStatus(Long userId) {
        User user = subscriptionPlanSyncService.syncUserPlan(userId);

        Subscription active = subscriptionRepository.findByUser_IdAndStatus(userId, SubscriptionStatus.ACTIVE).orElse(null);

        List<Payment> recent = paymentRepository.findByUser_IdOrderByCreatedAtDesc(userId, PageRequest.of(0, 3));
        List<SubscriptionStatusResponse.PaymentHistoryDto> history = recent.stream()
            .map(p -> SubscriptionStatusResponse.PaymentHistoryDto.builder()
                .portonePaymentId(p.getPortonePaymentId())
                .amount(p.getAmount())
                .status(p.getStatus().name())
                .paidAt(p.getPaidAt())
                .build())
            .collect(Collectors.toList());

        return SubscriptionStatusResponse.builder()
            .planType(user.getPlanType().name())
            .subscriptionStatus(active == null ? null : active.getStatus().name())
            .startedAt(active == null ? null : active.getStartedAt())
            .expiresAt(active == null ? null : active.getExpiresAt())
            .recentPayments(history)
            .build();
    }

    @Transactional
    public void cancelSubscription(Long userId) {
        Subscription active = subscriptionRepository.findByUser_IdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_ERROR, "사용자를 찾을 수 없습니다."));

        active.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(active);
        user.setPlanType(PlanType.FREE);
        userRepository.save(user);
        log.info("[Payment] Subscription cancelled: userId={}, subscriptionId={}", userId, active.getId());
    }

    private void expireActiveSubscriptions(Long userId) {
        subscriptionRepository.findByUser_IdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .ifPresent(active -> {
                active.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(active);
                log.info("[Payment] Previous active subscription expired: userId={}, subscriptionId={}", userId, active.getId());
            });
    }

    private void validatePortOneResponse(PaymentVerifyRequest request, PortOnePaymentResponse remote) {
        if (remote.getId() == null || remote.getAmount() == null || remote.getStatus() == null) {
            throw new CustomException(ErrorCode.PAYMENT_PORTONE_API_ERROR, "Incomplete payment payload from PortOne.");
        }

        if (!request.getPaymentId().equals(remote.getId())) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (!"paid".equalsIgnoreCase(remote.getStatus())) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_PAID);
        }

        if (!remote.getAmount().equals(subscriptionPrice)) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        String remoteOrder = remote.getOrderName();
        if (remoteOrder != null && request.getOrderName() != null && !remoteOrder.equals(request.getOrderName())) {
            log.warn("[Payment] Order name mismatch between client and PortOne: client={}, portone={}", request.getOrderName(), remoteOrder);
        }
    }

    private LocalDateTime parsePaidAt(String raw, LocalDateTime fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            if (raw.chars().allMatch(Character::isDigit)) {
                long epoch = Long.parseLong(raw);
                if (epoch > 1_000_000_000_000L) {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
                }
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
            }
            return LocalDateTime.ofInstant(Instant.parse(raw), ZoneOffset.UTC);
        } catch (Exception ex) {
            log.warn("[Payment] Could not parse paidAt={}, using fallback", raw);
            return fallback;
        }
    }
}
