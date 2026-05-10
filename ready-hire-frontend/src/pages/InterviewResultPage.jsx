import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getInterviewDetail } from '../api/interview.js'
import Navbar from '../components/Navbar.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'

/**
 * 완료된 면접의 종합 결과와 질문별 피드백을 보여줍니다.
 */
function InterviewResultPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [loading, setLoading] = useState(true)
  const [result, setResult] = useState(null)

  useEffect(() => {
    const fetchResult = async () => {
      try {
        setLoading(true)
        const response = await getInterviewDetail(id)
        setResult(response.data)
      } catch (error) {
        console.error('Failed to fetch interview result:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchResult()
  }, [id])

  const questionResults = useMemo(() => result?.questionResults ?? result?.questions ?? [], [result])
  const averageScore =
    result?.averageScore ??
    (questionResults.length
      ? Math.round(questionResults.reduce((sum, item) => sum + Number(item.score ?? 0), 0) / questionResults.length)
      : 0)

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <main className="mx-auto max-w-3xl px-4 py-8">
          <p className="text-sm text-gray-500">결과를 불러오는 중...</p>
        </main>
      </div>
    )
  }

  const isPro = String(user?.planType ?? 'FREE').toUpperCase() === 'PRO'

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-3xl space-y-6 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 text-center shadow-sm">
          <p className="text-sm text-gray-500">전체 평균 점수</p>
          <p className="mt-2 text-5xl font-bold text-primary">{averageScore}</p>
        </section>

        <section className="space-y-4">
          {questionResults.map((item, index) => (
            <article key={item.id ?? index} className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
              <p className="text-sm text-gray-500">질문 {index + 1}</p>
              <p className="mt-1 font-medium">{item.question ?? item.questionContent ?? '질문 정보 없음'}</p>
              <p className="mt-3 text-primary">점수: {item.score ?? 0}</p>
              {isPro ? (
                <div className="mt-4 space-y-2 rounded-xl bg-gray-50 p-3 text-sm text-gray-700">
                  <p>잘한점: {item.strengths ?? item.feedback?.strengths ?? '-'}</p>
                  <p>개선점: {item.improvements ?? item.feedback?.improvements ?? '-'}</p>
                  <p>모범답안: {item.modelAnswer ?? item.feedback?.modelAnswer ?? '-'}</p>
                </div>
              ) : (
                <div className="mt-4 rounded-xl bg-purple-50 p-3 text-sm text-purple-700">
                  <p>PRO 플랜에서 상세 피드백을 확인하세요.</p>
                  <button type="button" className="mt-2 rounded-lg bg-secondary px-3 py-2 text-white">
                    업그레이드
                  </button>
                </div>
              )}
            </article>
          ))}
        </section>

        <section className="flex gap-3">
          <button type="button" onClick={() => navigate('/interview/setup')} className="rounded-xl bg-primary px-6 py-3 font-medium text-white">
            다시 면접하기
          </button>
          <button type="button" onClick={() => navigate('/dashboard')} className="rounded-xl border border-gray-200 bg-white px-6 py-3 font-medium">
            홈으로
          </button>
        </section>
      </main>
    </div>
  )
}

export default InterviewResultPage
