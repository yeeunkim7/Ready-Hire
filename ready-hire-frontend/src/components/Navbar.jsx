import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext.jsx'
import PlanBadge from './PlanBadge.jsx'

/**
 * 로그인 이후 상단 네비게이션 바입니다.
 */
function Navbar() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <header className="border-b border-gray-100 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
        <button type="button" onClick={() => navigate('/dashboard')} className="text-lg font-semibold text-primary">
          Ready-Hire
        </button>
        <div className="flex items-center gap-3">
          <p className="hidden text-sm text-gray-600 sm:block">{user?.email}</p>
          <PlanBadge planType={user?.planType} />
          <button type="button" onClick={() => navigate('/subscription')} className="text-sm text-primary underline">
            구독 관리
          </button>
          <button type="button" onClick={handleLogout} className="rounded-xl border border-gray-200 px-4 py-2 text-sm">
            로그아웃
          </button>
        </div>
      </div>
    </header>
  )
}

export default Navbar
