export const SESSION_MODES = {
  PRACTICE: 'PRACTICE',
  EXAM: 'EXAM',
}

export const SESSION_MODE_LABELS = {
  [SESSION_MODES.PRACTICE]: '연습모드',
  [SESSION_MODES.EXAM]: '실전모드',
}

export const EXAM_SECONDS_PER_QUESTION = 90

export const TIMEOUT_ANSWER_PLACEHOLDER = '시간 초과로 답변을 제출하지 못했습니다.'

export const sessionModeStorageKey = (interviewId) => `interview_session_mode_${interviewId}`

export function computeRemainingSeconds(questionStartedAt, timeLimitSeconds) {
  if (!timeLimitSeconds) return timeLimitSeconds ?? EXAM_SECONDS_PER_QUESTION
  if (!questionStartedAt) return timeLimitSeconds
  const startedMs = new Date(questionStartedAt).getTime()
  if (Number.isNaN(startedMs)) return timeLimitSeconds
  const elapsed = Math.floor((Date.now() - startedMs) / 1000)
  return Math.max(0, timeLimitSeconds - elapsed)
}

export function mapApiQuestions(questions) {
  if (!Array.isArray(questions)) return []
  return questions.map((question) => ({
    id: question.id,
    order: question.order,
    content: question.content,
  }))
}
