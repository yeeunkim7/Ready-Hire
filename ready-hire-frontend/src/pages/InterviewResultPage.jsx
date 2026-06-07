import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getInterviewDetail } from '../api/interview.js'
import Button from '../components/ui/Button.jsx'
import Card from '../components/ui/Card.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { SESSION_MODE_LABELS } from '../constants/interviewSession.js'
import { getScoreColorClass } from '../utils/score.js'

const INTERVIEW_MODE_LABELS = {
  STANDARD: '기본 면접',
  JOB_POSTING: '채용공고 맞춤 면접',
  PORTFOLIO: '포트폴리오 맞춤 면접',
}

/**
 * 완료된 면접의 종합 결과와 질문별 피드백을 보여줍니다.
 */
function InterviewResultPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [loading, setLoading] = useState(true)
  const [result, setResult] = useState(null)

  useEffect(() => {
    const fetchResult = async () => {
      try {
        setLoading(true)
        const data = await getInterviewDetail(id)
        setResult(data)
      } catch {
        /* axios / unwrap */
      } finally {
        setLoading(false)
      }
    }
    fetchResult()
  }, [id])

  const questionResults = useMemo(() => result?.results ?? [], [result])
  const totalScore = result?.totalScore ?? 0
  const interviewMode =
    result?.interviewMode
    ?? location.state?.interviewMode
    ?? sessionStorage.getItem(`interview_mode_${id}`)
    ?? 'STANDARD'
  const sessionMode =
    result?.sessionMode
    ?? location.state?.sessionMode
    ?? sessionStorage.getItem(`interview_session_mode_${id}`)
    ?? 'PRACTICE'
  const modeLabel = INTERVIEW_MODE_LABELS[interviewMode] ?? INTERVIEW_MODE_LABELS.STANDARD
  const sessionModeLabel = SESSION_MODE_LABELS[sessionMode] ?? SESSION_MODE_LABELS.PRACTICE

  if (loading) {
    return <p className="text-sm text-gray-500">결과를 불러오는 중...</p>
  }

  const isPro = String(user?.planType ?? 'FREE').toUpperCase() === 'PRO'

  return (
    <>
      <Card className="text-center">
        <div className="flex flex-wrap items-center justify-center gap-2">
          <span className="inline-block rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-primary">
            {modeLabel}
          </span>
          <span className="inline-block rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
            {sessionModeLabel}
          </span>
        </div>
        <p className="mt-4 text-sm text-gray-500">전체 평균 점수</p>
        <p className={`mt-2 text-5xl font-bold ${getScoreColorClass(totalScore)}`}>{totalScore}</p>
      </Card>

      <section className="space-y-4">
        {questionResults.length === 0 && (
          <Card className="text-center text-sm text-gray-500">질문별 결과가 없습니다.</Card>
        )}
        {questionResults.map((item, index) => (
          <Card as="article" key={item.questionId ?? index}>
            <p className="text-sm text-gray-500">질문 {index + 1}</p>
            <p className="mt-1 font-medium">{item.questionContent ?? '질문 정보 없음'}</p>
            <p className={`mt-3 text-lg font-semibold ${getScoreColorClass(item.score)}`}>점수: {item.score ?? 0}</p>
            {isPro ? (
              <div className="mt-4 space-y-2 rounded-xl bg-gray-50 p-3 text-sm text-gray-700">
                <p>잘한점: {item.strengths ?? '-'}</p>
                <p>개선점: {item.improvements ?? '-'}</p>
                <p>모범답안: {item.modelAnswer ?? '-'}</p>
              </div>
            ) : (
              <div className="mt-4 rounded-xl border border-purple-100 bg-purple-50 p-4 text-sm text-purple-900">
                <p className="font-semibold">🔒 PRO 플랜에서 확인 가능</p>
                <ul className="mt-2 list-inside list-disc space-y-1 text-purple-800">
                  <li>잘한점 / 개선점 / 모범답안</li>
                </ul>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => navigate('/mypage#subscription')}
                  className="mt-3 w-full sm:w-auto"
                >
                  PRO 업그레이드 →
                </Button>
              </div>
            )}
          </Card>
        ))}
      </section>

      <section className="flex flex-wrap gap-3">
        <Button onClick={() => navigate('/interview/mode')}>다시 면접하기</Button>
        <Button variant="outline" onClick={() => navigate('/dashboard')}>
          홈으로
        </Button>
      </section>
    </>
  )
}

export default InterviewResultPage
