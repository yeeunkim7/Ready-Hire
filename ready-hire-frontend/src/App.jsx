import { Navigate, Route, Routes } from 'react-router-dom'
import PrivateRoute from './components/PrivateRoute.jsx'
import AppLayout from './layouts/AppLayout.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import InterviewPage from './pages/InterviewPage.jsx'
import InterviewResultPage from './pages/InterviewResultPage.jsx'
import InterviewModeSelectPage from './pages/InterviewModeSelectPage.jsx'
import InterviewSetupPage from './pages/InterviewSetupPage.jsx'
import MyPage from './pages/MyPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import SignupPage from './pages/SignupPage.jsx'
import OAuth2CallbackPage from './pages/OAuth2CallbackPage.jsx'
import SubscriptionPage from './pages/SubscriptionPage.jsx'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />

      {/* 대시보드 — 넓은 콘텐츠 */}
      <Route
        element={
          <PrivateRoute>
            <AppLayout maxWidth="5xl" />
          </PrivateRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardPage />} />
      </Route>

      {/* 면접 설정 플로우 — 중간 너비 */}
      <Route
        element={
          <PrivateRoute>
            <AppLayout maxWidth="2xl" />
          </PrivateRoute>
        }
      >
        <Route path="/interview/mode" element={<InterviewModeSelectPage />} />
        <Route path="/interview/setup" element={<InterviewSetupPage />} />
      </Route>

      {/* 면접 진행 — 집중형 좁은 너비 */}
      <Route
        element={
          <PrivateRoute>
            <AppLayout maxWidth="md" />
          </PrivateRoute>
        }
      >
        <Route path="/interview/:id" element={<InterviewPage />} />
      </Route>

      {/* 결과 / 마이페이지 — 본문 너비 */}
      <Route
        element={
          <PrivateRoute>
            <AppLayout maxWidth="3xl" />
          </PrivateRoute>
        }
      >
        <Route path="/interview/:id/result" element={<InterviewResultPage />} />
        <Route path="/mypage" element={<MyPage />} />
      </Route>

      <Route
        path="/subscription"
        element={
          <PrivateRoute>
            <SubscriptionPage />
          </PrivateRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
