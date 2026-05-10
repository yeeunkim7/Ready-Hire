import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'

/**
 * OAuth2 콜백에서 토큰을 저장하고 대시보드로 이동합니다.
 */
function OAuth2CallbackPage() {
  const navigate = useNavigate()
  const { login } = useAuth()

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
    } catch (error) {
      console.error('OAuth2 callback handling failed:', error)
      navigate('/login?error=oauth2_failed', { replace: true })
    }
  }, [login, navigate])

  return <LoadingSpinner fullScreen message="로그인 처리 중..." />
}

export default OAuth2CallbackPage
