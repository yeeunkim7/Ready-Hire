import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { withdraw } from '../api/auth.js'
import ConfirmModal from '../components/ConfirmModal.jsx'
import Navbar from '../components/Navbar.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'
import { getRefreshToken } from '../utils/token.js'

function AccountPage() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { showToast } = useToast()
  const [withdrawOpen, setWithdrawOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleWithdraw = async () => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      showToast('세션 정보를 찾을 수 없습니다.', 'error')
      return
    }

    try {
      setLoading(true)
      await withdraw(refreshToken)
      await logout()
      showToast('회원 탈퇴가 완료되었습니다.', 'success')
      navigate('/login', { replace: true })
    } catch {
      /* axios / unwrap */
    } finally {
      setLoading(false)
      setWithdrawOpen(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-lg space-y-6 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <h1 className="text-xl font-bold">계정 설정</h1>
          <p className="mt-2 text-sm text-gray-500">{user?.email}</p>
          <p className="mt-1 text-sm text-gray-600">플랜: {user?.planType ?? 'FREE'}</p>
        </section>

        <section className="rounded-2xl border border-red-100 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-red-700">회원 탈퇴</h2>
          <p className="mt-2 text-sm text-gray-600">
            탈퇴 시 계정이 비활성화되며 PRO 구독이 해지됩니다. 이 작업은 되돌릴 수 없습니다.
          </p>
          <button
            type="button"
            onClick={() => setWithdrawOpen(true)}
            className="mt-4 rounded-xl border border-red-200 px-4 py-2 text-sm font-medium text-red-700"
          >
            회원 탈퇴
          </button>
        </section>
      </main>

      <ConfirmModal
        isOpen={withdrawOpen}
        title="회원 탈퇴"
        message="정말 탈퇴하시겠습니까? 면접 기록은 유지되지만 계정으로 다시 로그인할 수 없습니다."
        confirmText={loading ? '처리 중...' : '탈퇴하기'}
        cancelText="취소"
        isDanger
        onConfirm={handleWithdraw}
        onCancel={() => setWithdrawOpen(false)}
      />
    </div>
  )
}

export default AccountPage
