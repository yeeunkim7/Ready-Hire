import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { completeInterview, getInterviewDetail, submitAnswer } from '../api/interview.js'
import {
  EXAM_SECONDS_PER_QUESTION,
  SESSION_MODES,
  SESSION_MODE_LABELS,
  TIMEOUT_ANSWER_PLACEHOLDER,
  sessionModeStorageKey,
} from '../constants/interviewSession.js'
import Navbar from '../components/Navbar.jsx'
import ProgressBar from '../components/ProgressBar.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

function formatSeconds(total) {
  const minutes = Math.floor(total / 60)
  const seconds = total % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

/**
 * 질문을 한 개씩 순차적으로 답변받아 제출하는 페이지입니다.
 */
function InterviewPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [questions, setQuestions] = useState([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [answer, setAnswer] = useState('')
  const [remainingSeconds, setRemainingSeconds] = useState(EXAM_SECONDS_PER_QUESTION)

  const sessionMode =
    location.state?.sessionMode
    ?? sessionStorage.getItem(sessionModeStorageKey(id))
    ?? SESSION_MODES.PRACTICE
  const isExamMode = sessionMode === SESSION_MODES.EXAM

  const answerRef = useRef(answer)
  const submittingRef = useRef(submitting)

  useEffect(() => {
    answerRef.current = answer
  }, [answer])

  useEffect(() => {
    submittingRef.current = submitting
  }, [submitting])

  useEffect(() => {
    const bootstrap = async () => {
      try {
        setLoading(true)
        const fromState = location.state?.questions
        let raw = Array.isArray(fromState) && fromState.length ? fromState : null
        if (!raw) {
          try {
            const cached = sessionStorage.getItem(`interview_questions_${id}`)
            if (cached) raw = JSON.parse(cached)
          } catch {
            raw = null
          }
        }
        if (raw?.length) {
          setQuestions(raw)
          return
        }

        const detail = await getInterviewDetail(id)
        if (String(detail?.status).toUpperCase() === 'COMPLETED') {
          navigate(`/interview/${id}/result`, { replace: true })
          return
        }
        showToast('면접 질문 정보를 찾을 수 없습니다. 면접 설정부터 다시 시작해 주세요.', 'error')
        navigate('/interview/mode', { replace: true })
      } catch {
        /* axios / unwrap */
      } finally {
        setLoading(false)
      }
    }
    bootstrap()
    // eslint-disable-next-line react-hooks/exhaustive-deps -- 초기 진입 시 질문 목록만 복원
  }, [id, navigate, showToast])

  const total = questions.length || 5
  const currentQuestion = useMemo(() => questions[currentIndex] ?? {}, [questions, currentIndex])

  const goNextOrComplete = useCallback(
    async (interviewMode) => {
      if (currentIndex + 1 >= total) {
        await completeInterview(id)
        sessionStorage.removeItem(`interview_questions_${id}`)
        navigate(`/interview/${id}/result`, { state: { interviewMode, sessionMode } })
        return
      }
      setCurrentIndex((prev) => prev + 1)
      setAnswer('')
    },
    [currentIndex, id, navigate, sessionMode, total],
  )

  const handleSubmit = useCallback(
    async ({ forced = false } = {}) => {
      if (submittingRef.current) return

      const trimmed = answerRef.current.trim()
      const content = trimmed || (forced ? TIMEOUT_ANSWER_PLACEHOLDER : '')

      if (!content) {
        showToast('답변을 입력해 주세요.', 'error')
        return
      }

      try {
        setSubmitting(true)
        await submitAnswer(id, {
          questionId: currentQuestion.id,
          content,
        })

        if (forced && !trimmed) {
          showToast('시간이 초과되어 답변이 자동 제출되었습니다.', 'error')
        }

        const interviewMode =
          location.state?.interviewMode ?? sessionStorage.getItem(`interview_mode_${id}`) ?? 'STANDARD'
        await goNextOrComplete(interviewMode)
      } catch {
        /* axios / unwrap */
      } finally {
        setSubmitting(false)
      }
    },
    [currentQuestion.id, goNextOrComplete, id, location.state?.interviewMode, showToast],
  )

  const handleSubmitRef = useRef(handleSubmit)
  useEffect(() => {
    handleSubmitRef.current = handleSubmit
  }, [handleSubmit])

  useEffect(() => {
    if (!isExamMode || loading || submitting) return undefined

    setRemainingSeconds(EXAM_SECONDS_PER_QUESTION)

    const interval = window.setInterval(() => {
      setRemainingSeconds((prev) => {
        if (prev <= 1) {
          window.clearInterval(interval)
          handleSubmitRef.current({ forced: true })
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => window.clearInterval(interval)
  }, [currentIndex, isExamMode, loading, submitting])

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="mx-auto max-w-md px-4 py-8">
          <p className="text-sm text-gray-500">면접 질문을 불러오는 중...</p>
        </main>
      </div>
    )
  }

  const timerUrgent = isExamMode && remainingSeconds <= 30

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-md space-y-4 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <span className="rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-primary">
              {SESSION_MODE_LABELS[sessionMode]}
            </span>
            {isExamMode && (
              <span className={`text-sm font-semibold ${timerUrgent ? 'text-red-600' : 'text-gray-700'}`}>
                남은 시간 {formatSeconds(remainingSeconds)}
              </span>
            )}
          </div>

          <ProgressBar current={Math.min(currentIndex + 1, total)} total={total} />
          <p className="mt-4 text-base font-medium">
            {currentQuestion.content ?? `${currentIndex + 1}번 질문을 준비 중입니다.`}
          </p>

          {isExamMode && (
            <p className="mt-2 text-xs text-gray-500">질문당 1분 30초 안에 답변해 주세요. 시간 초과 시 자동 제출됩니다.</p>
          )}

          <textarea
            className="mt-4 min-h-28 w-full rounded-xl border border-gray-200 p-3 text-sm focus:border-primary focus:outline-none"
            value={answer}
            onChange={(event) => setAnswer(event.target.value.slice(0, 500))}
            placeholder="답변을 입력하세요 (최대 500자)"
            rows={5}
            disabled={submitting}
          />
          <p className="mt-2 text-right text-xs text-gray-500">{answer.length}/500</p>
          <button
            type="button"
            onClick={() => handleSubmit()}
            disabled={submitting}
            className="mt-4 w-full rounded-xl bg-primary px-6 py-3 font-medium text-white disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {submitting ? '제출 중...' : currentIndex + 1 >= total ? '제출' : '다음 질문'}
          </button>
        </section>
      </main>
    </div>
  )
}

export default InterviewPage
