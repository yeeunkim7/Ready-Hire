package com.devinterview.api.domain.user.service;

import com.devinterview.api.auth.repository.UserRefreshTokenRepository;
import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.AccountStatus;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.enums.Provider;
import com.devinterview.api.domain.enums.SubscriptionStatus;
import com.devinterview.api.domain.interview.entity.Interview;
import com.devinterview.api.domain.interview.entity.InterviewResult;
import com.devinterview.api.domain.interview.repository.InterviewRepository;
import com.devinterview.api.domain.interview.repository.InterviewResultRepository;
import com.devinterview.api.domain.interview.service.InterviewService;
import com.devinterview.api.domain.payment.repository.SubscriptionRepository;
import com.devinterview.api.domain.payment.service.SubscriptionPlanSyncService;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.domain.user.dto.InterviewDetailResponse;
import com.devinterview.api.domain.user.dto.InterviewHistoryPageResponse;
import com.devinterview.api.domain.user.dto.InterviewHistoryResponse;
import com.devinterview.api.domain.user.dto.MyPageResponse;
import com.devinterview.api.domain.user.dto.UpdateUserRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final InterviewService interviewService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanSyncService subscriptionPlanSyncService;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        User user = getActiveUser(userId);
        subscriptionPlanSyncService.syncUserPlan(userId);

        List<InterviewHistoryResponse> recent = interviewRepository
            .findTop5ByUser_IdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toHistoryResponse)
            .collect(Collectors.toList());

        return MyPageResponse.builder()
            .email(user.getEmail())
            .planType(user.getPlanType().name())
            .provider(user.getProvider().name())
            .joinedAt(user.getCreatedAt())
            .recentInterviews(recent)
            .build();
    }

    @Transactional
    public void updateProfile(Long userId, UpdateUserRequest request) {
        User user = getActiveUser(userId);

        if (request.password() == null || request.password().isBlank()) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "변경할 비밀번호를 입력해 주세요.");
        }

        if (user.getProvider() != Provider.LOCAL) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "OAuth 계정은 비밀번호를 변경할 수 없습니다.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        log.info("[User] Password updated: userId={}", userId);
    }

    @Transactional
    public void withdraw(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.ACCOUNT_WITHDRAWN);
        }

        subscriptionRepository.findByUser_IdAndStatus(userId, SubscriptionStatus.ACTIVE)
            .ifPresent(subscription -> subscription.setStatus(SubscriptionStatus.CANCELLED));

        user.setAccountStatus(AccountStatus.WITHDRAWN);
        user.setPlanType(PlanType.FREE);
        user.setPasswordHash(null);
        user.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));

        userRefreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(token -> token.setRevoked(true));
        userRefreshTokenRepository.findByUserId(userId).ifPresent(token -> token.setRevoked(true));

        log.info("[User] Account withdrawn (soft delete): userId={}, provider={}", userId, user.getProvider());
    }

    @Transactional(readOnly = true)
    public InterviewHistoryPageResponse getInterviewHistory(Long userId, int page, int size) {
        getActiveUser(userId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<Interview> interviewPage = interviewRepository.findByUser_IdOrderByCreatedAtDesc(
            userId,
            PageRequest.of(safePage, safeSize)
        );

        List<InterviewHistoryResponse> content = interviewPage.getContent().stream()
            .map(this::toHistoryResponse)
            .collect(Collectors.toList());

        return InterviewHistoryPageResponse.builder()
            .content(content)
            .page(interviewPage.getNumber())
            .size(interviewPage.getSize())
            .totalElements(interviewPage.getTotalElements())
            .totalPages(interviewPage.getTotalPages())
            .build();
    }

    @Transactional(readOnly = true)
    public InterviewDetailResponse getInterviewDetail(Long userId, Long interviewId) {
        User user = getActiveUser(userId);
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));

        if (!interview.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.INTERVIEW_ACCESS_DENIED);
        }

        var complete = interviewService.getInterviewDetail(userId, interviewId);
        boolean isPro = user.getPlanType() == PlanType.PRO;

        List<InterviewDetailResponse.QuestionResult> results = complete.getResults() == null
            ? Collections.emptyList()
            : complete.getResults().stream()
                .map(result -> InterviewDetailResponse.QuestionResult.builder()
                    .questionId(result.getQuestionId())
                    .questionContent(result.getQuestionContent())
                    .score(result.getScore())
                    .strengths(isPro ? result.getStrengths() : null)
                    .improvements(isPro ? result.getImprovements() : null)
                    .modelAnswer(isPro ? result.getModelAnswer() : null)
                    .build())
                .collect(Collectors.toList());

        return InterviewDetailResponse.builder()
            .interviewId(interview.getId())
            .jobRole(interview.getJobRole())
            .techStack(interview.getTechStack())
            .experienceLevel(interview.getExperienceLevel())
            .status(interview.getStatus().name())
            .createdAt(interview.getCreatedAt())
            .totalScore(complete.getTotalScore())
            .results(results)
            .build();
    }

    private InterviewHistoryResponse toHistoryResponse(Interview interview) {
        Integer totalScore = resolveTotalScore(interview.getId());
        return InterviewHistoryResponse.builder()
            .interviewId(interview.getId())
            .jobRole(interview.getJobRole())
            .techStack(interview.getTechStack())
            .experienceLevel(interview.getExperienceLevel())
            .status(interview.getStatus().name())
            .createdAt(interview.getCreatedAt())
            .totalScore(totalScore)
            .build();
    }

    private Integer resolveTotalScore(Long interviewId) {
        List<InterviewResult> results = interviewResultRepository.findByInterviewIdOrderByQuestionId(interviewId);
        if (results.isEmpty()) {
            return null;
        }
        return (int) Math.round(
            results.stream()
                .map(InterviewResult::getScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0)
        );
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
        return user;
    }
}
