package com.devinterview.api.domain.interview.service;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;
import com.devinterview.api.domain.ai.service.ChatService;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.CareerLevel;
import com.devinterview.api.domain.enums.InterviewType;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.interview.dto.InterviewStartRequest;
import com.devinterview.api.domain.interview.dto.InterviewStartResponse;
import com.devinterview.api.domain.interview.dto.InterviewSummaryDto;
import com.devinterview.api.domain.interview.entity.Interview;
import com.devinterview.api.domain.interview.entity.InterviewQuestion;
import com.devinterview.api.domain.interview.entity.InterviewSessionStatus;
import com.devinterview.api.domain.interview.repository.InterviewQuestionRepository;
import com.devinterview.api.domain.interview.repository.InterviewRepository;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.domain.usage.entity.DailyUsage;
import com.devinterview.api.domain.usage.repository.DailyUsageRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 면접 세션 시작/조회 비즈니스를 처리하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final int FREE_DAILY_LIMIT = 3;
    private static final int QUESTION_COUNT = 5;

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final DailyUsageRepository dailyUsageRepository;
    private final ChatService chatService;

    @Transactional
    public InterviewStartResponse startInterview(Long userId, InterviewStartRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_ERROR, "사용자를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        DailyUsage dailyUsage = null;

        if (user.getPlanType() == PlanType.FREE) {
            dailyUsage = dailyUsageRepository.findByUserIdAndUsageDate(userId, today)
                .orElseGet(() -> DailyUsage.builder()
                    .user(user)
                    .usageDate(today)
                    .usageCount(0)
                    .build());

            log.info("[Interview] FREE usage check: userId={}, date={}, used={}", userId, today, dailyUsage.getUsageCount());
            if (dailyUsage.getUsageCount() >= FREE_DAILY_LIMIT) {
                throw new CustomException(ErrorCode.DAILY_LIMIT_EXCEEDED);
            }
        }

        Interview interview = interviewRepository.save(
            Interview.builder()
                .user(user)
                .jobRole(request.getJobRole())
                .techStack(request.getTechStack())
                .experienceLevel(request.getExperienceLevel())
                .status(InterviewSessionStatus.IN_PROGRESS)
                .build()
        );

        log.info("[Interview] Session started: interviewId={}, userId={}", interview.getId(), userId);

        QuestionGenerationResult generationResult;
        try {
            generationResult = chatService.generateQuestions(
                new QuestionGenerationCommand(
                    userId,
                    InterviewType.TECHNICAL,
                    parseCareerLevel(request.getExperienceLevel()),
                    request.getJobRole(),
                    null,
                    QUESTION_COUNT,
                    request.getTechStack()
                )
            );
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.QUESTION_GENERATION_FAILED, ex.getMessage());
        }

        List<String> generatedQuestions = generationResult == null ? Collections.emptyList() : generationResult.questions();
        if (generatedQuestions.isEmpty()) {
            throw new CustomException(ErrorCode.QUESTION_GENERATION_FAILED);
        }

        List<InterviewStartResponse.QuestionDto> questionDtos = new ArrayList<>();
        int order = 1;
        for (String question : generatedQuestions) {
            if (order > QUESTION_COUNT) {
                break;
            }
            InterviewQuestion saved = interviewQuestionRepository.save(
                InterviewQuestion.builder()
                    .interview(interview)
                    .questionOrder(order)
                    .content(question)
                    .build()
            );
            questionDtos.add(new InterviewStartResponse.QuestionDto(saved.getId(), order, saved.getContent()));
            order++;
        }

        if (user.getPlanType() == PlanType.FREE) {
            dailyUsage.setUsageCount(dailyUsage.getUsageCount() + 1);
            dailyUsageRepository.save(dailyUsage);
            log.info("[Interview] FREE usage incremented: userId={}, date={}, used={}", userId, today, dailyUsage.getUsageCount());
        }

        log.info("[Interview] Question generation completed: interviewId={}, questionCount={}", interview.getId(), questionDtos.size());
        return new InterviewStartResponse(interview.getId(), questionDtos);
    }

    @Transactional(readOnly = true)
    public List<InterviewSummaryDto> getHistory(Long userId) {
        return Collections.emptyList();
    }

    private CareerLevel parseCareerLevel(String level) {
        if (level == null || level.isBlank()) {
            return CareerLevel.JUNIOR;
        }
        String normalized = level.trim().toUpperCase();
        return switch (normalized) {
            case "JUNIOR", "ENTRY", "NEWBIE" -> CareerLevel.JUNIOR;
            case "MID", "MIDDLE" -> CareerLevel.MID;
            case "SENIOR" -> CareerLevel.SENIOR;
            case "LEAD", "STAFF" -> CareerLevel.LEAD;
            default -> CareerLevel.JUNIOR;
        };
    }
}
