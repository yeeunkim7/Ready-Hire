import { Navigate, useLocation } from 'react-router-dom'

/**
 * 구독 관리는 마이페이지로 통합되었습니다. 기존 링크 호환용 리다이렉트.
 */
function SubscriptionPage() {
  const location = useLocation()
  return <Navigate to={`/mypage${location.hash || '#subscription'}`} replace />
}

export default SubscriptionPage
