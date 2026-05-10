import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { completeInterview, getInterviewDetail, submitAnswer } from '../api/interview.js'
import Navbar from '../components/Navbar.jsx'
import ProgressBar from '../components/ProgressBar.jsx'

/**
 * 질문을 한 개씩 순차적으로 답변받아 제출하는 페이지입니다.
 */
function InterviewPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [questions, setQuestions] = useState([])
  const [currentIndex, setCurrentIndex] = useState(0)
  const [answer, setAnswer] = useState('')

  useEffect(() => {
    const bootstrap = async () => {
      try {
        setLoading(true)
        const stateInterview = location.state?.interview
        if (stateInterview?.questions?.length) {
          setQuestions(stateInterview.questions)
          return
        }
        const response = await getInterviewDetail(id)
        const payload = response.data ?? {}
        setQuestions(payload.questions ?? payload.questionResults ?? [])
      } catch (error) {
        console.error('Failed to fetch interview detail:', error)
      } finally {
        setLoading(false)
      }
    }
    bootstrap()
  }, [id, location.state])

  const total = questions.length || 5
  const currentQuestion = useMemo(() => questions[currentIndex] ?? {}, [questions, currentIndex])

  const handleSubmit = async () => {
    if (!answer.trim()) {
      alert('답변을 입력해 주세요.')
      return
    }
    try {
      setSubmitting(true)
      await submitAnswer(id, {
        questionId: currentQuestion.id,
        questionOrder: currentIndex + 1,
        answer: answer.trim(),
      })

      if (currentIndex + 1 >= total) {
        await completeInterview(id)
        navigate(`/interview/${id}/result`)
        return
      }

      setCurrentIndex((prev) => prev + 1)
      setAnswer('')
    } catch (error) {
      console.error('Failed to submit answer:', error)
      alert('답변 제출에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

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

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-md space-y-4 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <ProgressBar current={Math.min(currentIndex + 1, total)} total={total} />
          <p className="mt-4 text-base font-medium">
            {currentQuestion.content ?? currentQuestion.question ?? `${currentIndex + 1}번 질문을 준비 중입니다.`}
          </p>
          <textarea
            className="mt-4 min-h-28 w-full rounded-xl border border-gray-200 p-3 text-sm focus:border-primary focus:outline-none"
            value={answer}
            onChange={(event) => setAnswer(event.target.value.slice(0, 500))}
            placeholder="답변을 입력하세요 (최대 500자)"
            rows={5}
          />
          <p className="mt-2 text-right text-xs text-gray-500">{answer.length}/500</p>
          <button
            type="button"
            onClick={handleSubmit}
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
