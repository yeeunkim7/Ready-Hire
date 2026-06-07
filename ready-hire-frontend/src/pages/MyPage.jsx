import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getMyInterviews, getMyPage, updateMyProfile, withdrawAccount } from '../api/user.js'
import ConfirmModal from '../components/ConfirmModal.jsx'
import Navbar from '../components/Navbar.jsx'
import PlanBadge from '../components/PlanBadge.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'
import { getRefreshToken } from '../utils/token.js'

function MyPage() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { showToast } = useToast()

  const [loading, setLoading] = useState(true)
  const [myPage, setMyPage] = useState(null)
  const [history, setHistory] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const [password, setPassword] = useState('')
  const [passwordSaving, setPasswordSaving] = useState(false)

  const [withdrawOpen, setWithdrawOpen] = useState(false)
  const [withdrawing, setWithdrawing] = useState(false)

  const loadMyPage = useCallback(async () => {
    const data = await getMyPage()
    setMyPage(data)
  }, [])

  const loadHistory = useCallback(async (nextPage = 0) => {
    const data = await getMyInterviews(nextPage, 10)
    setHistory(data?.content ?? [])
    setPage(data?.page ?? 0)
    setTotalPages(data?.totalPages ?? 0)
  }, [])

  useEffect(() => {
    const init = async () => {
      try {
        setLoading(true)
        await Promise.all([loadMyPage(), loadHistory(0)])
      } catch {
        /* axios / unwrap */
      } finally {
        setLoading(false)
      }
    }
    init()
  }, [loadMyPage, loadHistory])

  const handlePasswordUpdate = async (event) => {
    event.preventDefault()
    if (!password || password.length < 8) {
      showToast('비밀번호는 8자 이상이어야 합니다.', 'error')
      return
    }
    try {
      setPasswordSaving(true)
      await updateMyProfile({ password })
      setPassword('')
      showToast('비밀번호가 변경되었습니다.', 'success')
    } catch {
      /* axios / unwrap */
    } finally {
      setPasswordSaving(false)
    }
  }

  const handleWithdraw = async () => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      showToast('세션 정보를 찾을 수 없습니다.', 'error')
      return
    }
    try {
      setWithdrawing(true)
      await withdrawAccount(refreshToken)
      await logout()
      showToast('회원 탈퇴가 완료되었습니다.', 'success')
      navigate('/login', { replace: true })
    } catch {
      /* axios / unwrap */
    } finally {
      setWithdrawing(false)
      setWithdrawOpen(false)
    }
  }

  const planType = myPage?.planType ?? user?.planType
  const isLocal = String(myPage?.provider ?? '').toUpperCase() === 'LOCAL'

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-3xl space-y-6 px-4 py-6">
        {loading ? (
          <p className="text-sm text-gray-500">마이페이지를 불러오는 중...</p>
        ) : (
          <>
            <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h1 className="text-xl font-bold">마이페이지</h1>
                  <p className="mt-2 text-sm text-gray-600">{myPage?.email ?? user?.email}</p>
                  <p className="mt-1 text-sm text-gray-500">
                    가입일 {myPage?.joinedAt ? new Date(myPage.joinedAt).toLocaleDateString() : '-'}
                  </p>
                </div>
                <PlanBadge planType={planType} />
              </div>
            </section>

            {isLocal && (
              <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
                <h2 className="text-lg font-semibold">비밀번호 변경</h2>
                <form className="mt-4 space-y-3" onSubmit={handlePasswordUpdate}>
                  <input
                    type="password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    placeholder="새 비밀번호 (8자 이상)"
                    minLength={8}
                    className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm"
                  />
                  <button
                    type="submit"
                    disabled={passwordSaving}
                    className="rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white disabled:bg-indigo-300"
                  >
                    {passwordSaving ? '저장 중...' : '비밀번호 변경'}
                  </button>
                </form>
              </section>
            )}

            <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
              <h2 className="text-lg font-semibold">면접 히스토리</h2>
              {history.length === 0 ? (
                <p className="mt-4 text-sm text-gray-500">아직 면접 기록이 없습니다.</p>
              ) : (
                <ul className="mt-4 space-y-3">
                  {history.map((item) => (
                    <li key={item.interviewId} className="rounded-xl border border-gray-100 p-4">
                      <button
                        type="button"
                        className="flex w-full items-center justify-between text-left"
                        onClick={() => navigate(`/interview/${item.interviewId}/result`)}
                      >
                        <div>
                          <p className="font-medium">{item.jobRole ?? '직무 미지정'}</p>
                          <p className="text-sm text-gray-500">
                            {item.techStack?.length ? item.techStack.join(', ') : '기술스택 없음'}
                          </p>
                          <p className="text-xs text-gray-400">
                            {item.createdAt ? new Date(item.createdAt).toLocaleString() : ''}
                          </p>
                        </div>
                        <div className="text-right text-sm">
                          <p className="font-semibold text-primary">
                            {item.totalScore != null ? `${item.totalScore}점` : '-'}
                          </p>
                          <p className="text-gray-500">{item.status}</p>
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              {totalPages > 1 && (
                <div className="mt-4 flex items-center justify-center gap-2">
                  <button
                    type="button"
                    disabled={page <= 0}
                    onClick={() => loadHistory(page - 1)}
                    className="rounded-lg border border-gray-200 px-3 py-1 text-sm disabled:opacity-40"
                  >
                    이전
                  </button>
                  <span className="text-sm text-gray-500">
                    {page + 1} / {totalPages}
                  </span>
                  <button
                    type="button"
                    disabled={page >= totalPages - 1}
                    onClick={() => loadHistory(page + 1)}
                    className="rounded-lg border border-gray-200 px-3 py-1 text-sm disabled:opacity-40"
                  >
                    다음
                  </button>
                </div>
              )}
            </section>

            <section className="rounded-2xl border border-red-100 bg-white p-6 shadow-sm">
              <h2 className="text-lg font-semibold text-red-700">회원 탈퇴</h2>
              <p className="mt-2 text-sm text-gray-600">
                탈퇴 시 계정이 비활성화되며 PRO 구독이 해지됩니다. 면접 기록은 보관되지만 다시 로그인할 수 없습니다.
              </p>
              <button
                type="button"
                onClick={() => setWithdrawOpen(true)}
                className="mt-4 rounded-xl border border-red-200 px-4 py-2 text-sm font-medium text-red-700"
              >
                회원 탈퇴
              </button>
            </section>
          </>
        )}
      </main>

      <ConfirmModal
        isOpen={withdrawOpen}
        title="회원 탈퇴"
        message="정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다."
        confirmText={withdrawing ? '처리 중...' : '탈퇴하기'}
        cancelText="취소"
        isDanger
        onConfirm={handleWithdraw}
        onCancel={() => setWithdrawOpen(false)}
      />
    </div>
  )
}

export default MyPage
