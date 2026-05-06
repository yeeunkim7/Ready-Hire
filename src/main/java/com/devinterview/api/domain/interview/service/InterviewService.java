package com.devinterview.api.domain.interview.service;

import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisCommand;
import com.devinterview.api.domain.ai.dto.AnswerAnalysisResult;
import com.devinterview.api.domain.ai.dto.QuestionGenerationCommand;
import com.devinterview.api.domain.ai.dto.QuestionGenerationResult;
import com.devinterview.api.domain.ai.service.ChatService;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.CareerLevel;
import com.devinterview.api.domain.enums.InterviewType;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.interview.dto.AnswerSubmitRequest;
import com.devinterview.api.domain.interview.dto.AnswerSubmitResponse;
import com.devinterview.api.domain.interview.dto.InterviewCompleteResponse;
import com.devinterview.api.domain.interview.dto.InterviewStartRequest;
import com.devinterview.api.domain.interview.dto.InterviewStartResponse;
import com.devinterview.api.domain.interview.dto.InterviewSummaryDto;
import com.devinterview.api.domain.interview.entity.InterviewAnswer;
import com.devinterview.api.domain.interview.entity.Interview;
import com.devinterview.api.domain.interview.entity.InterviewQuestion;
import com.devinterview.api.domain.interview.entity.InterviewResult;
import com.devinterview.api.domain.interview.entity.InterviewSessionStatus;
import com.devinterview.api.domain.interview.repository.InterviewAnswerRepository;
import com.devinterview.api.domain.interview.repository.InterviewQuestionRepository;
import com.devinterview.api.domain.interview.repository.InterviewResultRepository;
import com.devinterview.api.domain.interview.repository.InterviewRepository;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.domain.usage.entity.DailyUsage;
import com.devinterview.api.domain.usage.repository.DailyUsageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final DailyUsageRepository dailyUsageRepository;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

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
        return interviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(InterviewSummaryDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 답변 저장 후 AI 분석 결과를 생성한다.
     */
    @Transactional
    public AnswerSubmitResponse submitAnswer(Long userId, Long interviewId, AnswerSubmitRequest request) {
        User user = getUserOrThrow(userId);
        Interview interview = getOwnedInterviewOrThrow(userId, interviewId);

        if (interview.getStatus() == InterviewSessionStatus.COMPLETED) {
            throw new CustomException(ErrorCode.INTERVIEW_ALREADY_COMPLETED);
        }

        InterviewQuestion question = interviewQuestionRepository.findById(request.getQuestionId())
            .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND, "질문 정보를 찾을 수 없습니다."));
        if (!question.getInterview().getId().equals(interviewId)) {
            throw new CustomException(ErrorCode.INTERVIEW_NOT_FOUND, "면접에 속하지 않는 질문입니다.");
        }

        InterviewAnswer answer = interviewAnswerRepository.save(
            InterviewAnswer.builder()
                .interview(interview)
                .question(question)
                .content(request.getContent())
                .build()
        );
        log.info("[Interview] Answer submitted: interviewId={}, questionId={}, answerId={}", interviewId, question.getId(), answer.getId());

        AnswerAnalysisResult analysisResult;
        try {
            log.info("[Interview] Analyze answer requested: interviewId={}, questionId={}", interviewId, question.getId());
            analysisResult = chatService.analyzeAnswer(
                new AnswerAnalysisCommand(
                    userId,
                    interviewId,
                    question.getId(),
                    question.getContent(),
                    request.getContent(),
                    "Technical depth, clarity, and problem-solving ability"
                )
            );
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.ANSWER_ANALYSIS_FAILED, ex.getMessage());
        }

        boolean isPro = user.getPlanType() == PlanType.PRO;
        ParsedFeedback parsedFeedback = parseFeedback(analysisResult.detailedFeedbackJson());
        String detailedFeedback = isPro ? buildDetailedFeedbackJson(parsedFeedback) : null;
        log.info("[Interview] Plan branch applied: userId={}, planType={}, isPro={}", userId, user.getPlanType(), isPro);

        InterviewResult result = interviewResultRepository.save(
            InterviewResult.builder()
                .interview(interview)
                .question(question)
                .answer(answer)
                .score(analysisResult.score())
                .detailedFeedback(detailedFeedback)
                .build()
        );

        return AnswerSubmitResponse.builder()
            .answerId(answer.getId())
            .resultId(result.getId())
            .score(analysisResult.score())
            .strengths(isPro ? parsedFeedback.strengths() : null)
            .improvements(isPro ? parsedFeedback.improvements() : null)
            .modelAnswer(isPro ? parsedFeedback.modelAnswer() : null)
            .isPro(isPro)
            .build();
    }

    /**
     * 면접 상태를 완료로 전환하고 결과를 반환한다.
     */
    @Transactional
    public InterviewCompleteResponse completeInterview(Long userId, Long interviewId) {
        Interview interview = getOwnedInterviewOrThrow(userId, interviewId);
        interview.setStatus(InterviewSessionStatus.COMPLETED);
        interviewRepository.save(interview);
        return buildInterviewDetailResponse(userId, interview);
    }

    /**
     * 면접 상세 결과를 조회한다.
     */
    @Transactional(readOnly = true)
    public InterviewCompleteResponse getInterviewDetail(Long userId, Long interviewId) {
        Interview interview = getOwnedInterviewOrThrow(userId, interviewId);
        return buildInterviewDetailResponse(userId, interview);
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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_ERROR, "사용자를 찾을 수 없습니다."));
    }

    private Interview getOwnedInterviewOrThrow(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));
        if (!interview.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.INTERVIEW_ACCESS_DENIED);
        }
        return interview;
    }

    private InterviewCompleteResponse buildInterviewDetailResponse(Long userId, Interview interview) {
        User user = getUserOrThrow(userId);
        boolean isPro = user.getPlanType() == PlanType.PRO;

        List<InterviewResult> results = interviewResultRepository.findByInterviewIdOrderByQuestionId(interview.getId());
        int totalScore = (int) Math.round(
            results.stream()
                .map(InterviewResult::getScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0)
        );

        List<InterviewCompleteResponse.ResultSummary> summaries = results.stream()
            .map(result -> {
                ParsedFeedback parsedFeedback = parseFeedback(result.getDetailedFeedback());
                return InterviewCompleteResponse.ResultSummary.builder()
                    .questionId(result.getQuestion().getId())
                    .questionContent(result.getQuestion().getContent())
                    .score(result.getScore())
                    .strengths(isPro ? parsedFeedback.strengths() : null)
                    .improvements(isPro ? parsedFeedback.improvements() : null)
                    .modelAnswer(isPro ? parsedFeedback.modelAnswer() : null)
                    .build();
            })
            .collect(Collectors.toList());

        return InterviewCompleteResponse.builder()
            .interviewId(interview.getId())
            .status(interview.getStatus().name())
            .totalScore(totalScore)
            .results(summaries)
            .build();
    }

    private String buildDetailedFeedbackJson(ParsedFeedback parsedFeedback) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("strengths", parsedFeedback.strengths());
        payload.put("improvements", parsedFeedback.improvements());
        payload.put("modelAnswer", parsedFeedback.modelAnswer());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new CustomException(ErrorCode.ANSWER_ANALYSIS_FAILED, "피드백 저장 형식 변환에 실패했습니다.");
        }
    }

    private ParsedFeedback parseFeedback(String detailedFeedbackJson) {
        if (detailedFeedbackJson == null || detailedFeedbackJson.isBlank()) {
            return new ParsedFeedback(null, null, null);
        }
        try {
            JsonNode node = objectMapper.readTree(detailedFeedbackJson);
            String strengths = stringifyField(node, "strengths");
            String improvements = stringifyField(node, "improvements");
            String modelAnswer = stringifyField(node, "modelAnswer");
            if (modelAnswer == null) {
                modelAnswer = stringifyField(node, "nextStep");
            }
            return new ParsedFeedback(strengths, improvements, modelAnswer);
        } catch (Exception ex) {
            log.warn("[Interview] Failed to parse detailed feedback json: {}", ex.getMessage());
            return new ParsedFeedback(null, null, null);
        }
    }

    private String stringifyField(JsonNode node, String fieldName) {
        JsonNode target = node.path(fieldName);
        if (target.isMissingNode() || target.isNull()) {
            return null;
        }
        if (target.isArray()) {
            List<String> values = new ArrayList<>();
            target.forEach(item -> {
                if (!item.isNull()) {
                    values.add(item.asText());
                }
            });
            return values.isEmpty() ? null : String.join(", ", values);
        }
        String text = target.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private record ParsedFeedback(
        String strengths,
        String improvements,
        String modelAnswer
    ) {
    }
}
