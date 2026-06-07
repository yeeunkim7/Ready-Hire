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
