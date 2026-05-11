import { useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

/**
 * Google OAuth2 로그인을 시작하는 진입 페이지입니다.
 */
function LoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { isLoggedIn } = useAuth()
  const { showToast } = useToast()

  useEffect(() => {
    if (isLoggedIn) {
      navigate('/dashboard', { replace: true })
    }
  }, [isLoggedIn, navigate])

  const error = new URLSearchParams(location.search).get('error')

  useEffect(() => {
    if (error === 'oauth2_failed') {
      showToast('로그인에 실패했습니다. 다시 시도해 주세요.', 'error')
      navigate('/login', { replace: true })
    }
  }, [error, navigate, showToast])

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <section className="w-full max-w-md rounded-2xl border border-gray-100 bg-white p-8 shadow-sm">
        <div className="mb-8 text-center">
          <p className="text-sm font-medium text-primary">AI MOCK INTERVIEW</p>
          <h1 className="mt-2 text-3xl font-bold text-gray-900">Ready-Hire</h1>
        </div>

        <button
          type="button"
          className="w-full rounded-xl bg-primary px-6 py-3 font-medium text-white transition hover:bg-indigo-700"
          onClick={() => {
            window.location.href = '/oauth2/authorization/google'
          }}
        >
          Google로 시작하기
        </button>

        <div className="mt-6 space-y-2 text-sm text-gray-500">
          <p>AI 면접 질문을 실제처럼 경험해 보세요.</p>
          <p>실시간 피드백으로 답변 완성도를 높일 수 있어요.</p>
          <p>면접 히스토리를 모아 성장 흐름을 추적하세요.</p>
        </div>
      </section>
    </main>
  )
}

export default LoginPage
