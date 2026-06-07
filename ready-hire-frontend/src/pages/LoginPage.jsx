import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login as loginApi } from '../api/auth.js'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

function LoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { isLoggedIn, login } = useAuth()
  const { showToast } = useToast()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

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

  const handleEmailLogin = async (event) => {
    event.preventDefault()
    try {
      setLoading(true)
      const tokens = await loginApi({ email, password })
      login(tokens.accessToken, tokens.refreshToken)
      navigate('/dashboard', { replace: true })
    } catch {
      /* axios / unwrap */
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <section className="w-full max-w-md rounded-2xl border border-gray-100 bg-white p-8 shadow-sm">
        <div className="mb-8 text-center">
          <p className="text-sm font-medium text-primary">AI MOCK INTERVIEW</p>
          <h1 className="mt-2 text-3xl font-bold text-gray-900">Ready-Hire</h1>
        </div>

        <form className="space-y-4" onSubmit={handleEmailLogin}>
          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-medium">이메일</label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="w-full rounded-xl border border-gray-200 px-3 py-2"
            />
          </div>
          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-medium">비밀번호</label>
            <input
              id="password"
              type="password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="w-full rounded-xl border border-gray-200 px-3 py-2"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-primary px-6 py-3 font-medium text-white disabled:bg-indigo-300"
          >
            {loading ? '로그인 중...' : '이메일로 로그인'}
          </button>
        </form>

        <div className="my-6 flex items-center gap-3 text-sm text-gray-400">
          <div className="h-px flex-1 bg-gray-200" />
          <span>또는</span>
          <div className="h-px flex-1 bg-gray-200" />
        </div>

        <button
          type="button"
          className="w-full rounded-xl border border-gray-200 bg-white px-6 py-3 font-medium text-gray-800"
          onClick={() => {
            window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`
          }}
        >
          Google로 시작하기
        </button>

        <p className="mt-6 text-center text-sm text-gray-500">
          계정이 없으신가요?{' '}
          <Link to="/signup" className="font-medium text-primary underline">회원가입</Link>
        </p>
      </section>
    </main>
  )
}

export default LoginPage
