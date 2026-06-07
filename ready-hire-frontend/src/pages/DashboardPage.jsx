import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getInterviewHistory } from '../api/interview.js'
import { getTodayUsage } from '../api/usage.js'
import PlanBadge from '../components/PlanBadge.jsx'
import Button from '../components/ui/Button.jsx'
import Card from '../components/ui/Card.jsx'
import Section from '../components/ui/Section.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'

/**
 * 사용자 요약 정보와 최근 면접 기록을 보여주는 대시보드입니다.
 */
function DashboardPage() {
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()
  const [loading, setLoading] = useState(true)
  const [history, setHistory] = useState([])
  const [remainingFreeCount, setRemainingFreeCount] = useState(3)

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setLoading(true)
        const [items, usage] = await Promise.all([getInterviewHistory(), getTodayUsage()])
        setHistory(Array.isArray(items) ? items : [])
        if (usage?.planType) {
          updateUser({ planType: usage.planType })
        }
        if (usage?.unlimited) {
          setRemainingFreeCount(0)
        } else {
          setRemainingFreeCount(usage?.remainingCount ?? 3)
        }
      } catch {
        /* 토스트는 axios / unwrap */
      } finally {
        setLoading(false)
      }
    }
    fetchDashboard()
  }, [updateUser])

  const isFree = String(user?.planType ?? 'FREE').toUpperCase() !== 'PRO'

  return (
    <>
      <Section className="bg-gradient-to-br from-white to-indigo-50/60">
        <div className="flex flex-wrap items-center justify-between gap-6">
          <div className="max-w-md space-y-3">
            <p className="text-sm font-medium text-primary">AI MOCK INTERVIEW</p>
            <h1 className="text-2xl font-bold text-gray-900">면접, 부담 없이 연습해요 👋</h1>
            <p className="text-sm text-gray-600">
              오늘도 한 걸음씩! Ready-Hire와 함께 면접 감각을 키워보세요.
            </p>
            <Button onClick={() => navigate('/interview/mode')}>새 면접 시작 →</Button>
          </div>
          {/* 마스코트 자리 (추후 이미지로 교체) */}
          <div
            aria-hidden
            className="flex h-32 w-32 shrink-0 items-center justify-center rounded-2xl bg-indigo-50 text-6xl shadow-sm"
          >
            🐥
          </div>
        </div>
      </Section>

      {isFree && (
        <Section className="border-amber-200 bg-amber-50">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-amber-900">
              PRO로 업그레이드하면 무제한 면접과 상세 AI 피드백을 이용할 수 있어요.
            </p>
            <Button
              variant="outline"
              size="sm"
              className="border-amber-300 bg-white text-amber-900 hover:bg-amber-100"
              onClick={() => navigate('/mypage#subscription')}
            >
              PRO로 업그레이드
            </Button>
          </div>
        </Section>
      )}

      <Section>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-sm text-gray-500">로그인 계정</p>
            <p className="text-lg font-semibold">{user?.email}</p>
          </div>
          <PlanBadge planType={user?.planType} />
        </div>
        {isFree && (
          <p className="mt-4 rounded-xl bg-gray-50 p-3 text-sm text-gray-600">오늘 남은 횟수 {remainingFreeCount}/3회</p>
        )}
      </Section>

      <Section title="최근 면접 히스토리">
        {loading ? (
          <p className="text-sm text-gray-500">불러오는 중...</p>
        ) : history.length === 0 ? (
          <p className="text-sm text-gray-500">아직 면접 기록이 없습니다.</p>
        ) : (
          <>
            <ul className="space-y-3">
              {history.map((item) => (
                <li key={item.interviewId ?? item.id} className="rounded-2xl border border-gray-100 p-4">
                  <button
                    type="button"
                    className="flex w-full items-center justify-between text-left"
                    onClick={() => navigate(`/interview/${item.interviewId ?? item.id}/result`)}
                  >
                    <div>
                      <p className="font-medium">{item.jobRole ?? item.position ?? '직무 미지정'}</p>
                      <p className="text-sm text-gray-500">
                        {item.createdAt ? new Date(item.createdAt).toLocaleString() : '날짜 정보 없음'}
                      </p>
                    </div>
                    <span className="text-sm text-gray-600">{item.status ?? 'COMPLETED'}</span>
                  </button>
                </li>
              ))}
            </ul>
            {isFree && history.length >= 3 && (
              <div className="mt-4 rounded-2xl border border-indigo-100 bg-indigo-50 px-4 py-3 text-sm text-indigo-900">
                <p>📋 최근 3건만 표시됩니다. 전체 히스토리는 PRO 플랜에서 확인하세요.</p>
                <button
                  type="button"
                  onClick={() => navigate('/mypage#subscription')}
                  className="mt-2 font-semibold text-primary underline"
                >
                  PRO 업그레이드 →
                </button>
              </div>
            )}
          </>
        )}
      </Section>
    </>
  )
}

export default DashboardPage
