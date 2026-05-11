import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

/**
 * OAuth2 콜백에서 토큰을 저장하고 대시보드로 이동합니다.
 */
function OAuth2CallbackPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const { showToast } = useToast()

  useEffect(() => {
    try {
      const params = new URLSearchParams(window.location.search)
      const accessToken = params.get('accessToken')
      const refreshToken = params.get('refreshToken')

      if (!accessToken || !refreshToken) {
        navigate('/login?error=oauth2_failed', { replace: true })
        return
      }

      login(accessToken, refreshToken)
      navigate('/dashboard', { replace: true })
    } catch {
      showToast('로그인 처리 중 오류가 발생했습니다.', 'error')
      navigate('/login?error=oauth2_failed', { replace: true })
    }
  }, [login, navigate, showToast])

  return <LoadingSpinner fullScreen message="로그인 처리 중..." />
}

export default OAuth2CallbackPage
