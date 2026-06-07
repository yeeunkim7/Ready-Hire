import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getInterviewHistory } from '../api/interview.js'
import { getTodayUsage } from '../api/usage.js'
import Navbar from '../components/Navbar.jsx'
import PlanBadge from '../components/PlanBadge.jsx'
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
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      {isFree && (
        <div className="sticky top-0 z-40 border-b border-amber-200 bg-amber-100 px-4 py-3 text-center text-sm text-amber-900">
          <button type="button" className="font-semibold underline" onClick={() => navigate('/subscription')}>
            PRO로 업그레이드
          </button>
          <span className="hidden sm:inline"> — 무제한 면접과 상세 AI 피드백을 이용해 보세요.</span>
        </div>
      )}
      <main className="mx-auto max-w-5xl space-y-6 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-sm text-gray-500">로그인 계정</p>
              <p className="text-lg font-semibold">{user?.email}</p>
            </div>
            <PlanBadge planType={user?.planType} />
          </div>
          {String(user?.planType ?? 'FREE').toUpperCase() !== 'PRO' && (
            <p className="mt-4 rounded-xl bg-gray-50 p-3 text-sm text-gray-600">오늘 남은 횟수 {remainingFreeCount}/3회</p>
          )}
          <button
            type="button"
            onClick={() => navigate('/interview/setup')}
            className="mt-4 rounded-xl bg-primary px-6 py-3 font-medium text-white"
          >
            새 면접 시작
          </button>
        </section>

        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">최근 면접 히스토리</h2>
          {loading ? (
            <p className="mt-4 text-sm text-gray-500">불러오는 중...</p>
          ) : history.length === 0 ? (
            <p className="mt-4 text-sm text-gray-500">아직 면접 기록이 없습니다.</p>
          ) : (
            <>
              <ul className="mt-4 space-y-3">
                {history.map((item) => (
                  <li key={item.interviewId ?? item.id} className="rounded-xl border border-gray-100 p-4">
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
                <div className="mt-4 rounded-xl border border-indigo-100 bg-indigo-50 px-4 py-3 text-sm text-indigo-900">
                  <p>📋 최근 3건만 표시됩니다. 전체 히스토리는 PRO 플랜에서 확인하세요.</p>
                  <button
                    type="button"
                    onClick={() => navigate('/subscription')}
                    className="mt-2 font-semibold text-primary underline"
                  >
                    PRO 업그레이드 →
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </main>
    </div>
  )
}

export default DashboardPage
