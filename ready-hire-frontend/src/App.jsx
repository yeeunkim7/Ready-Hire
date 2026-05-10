import { Navigate, Route, Routes } from 'react-router-dom'
import PrivateRoute from './components/PrivateRoute.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import InterviewPage from './pages/InterviewPage.jsx'
import InterviewResultPage from './pages/InterviewResultPage.jsx'
import InterviewSetupPage from './pages/InterviewSetupPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import OAuth2CallbackPage from './pages/OAuth2CallbackPage.jsx'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
      <Route
        path="/dashboard"
        element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/interview/setup"
        element={
          <PrivateRoute>
            <InterviewSetupPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/interview/:id"
        element={
          <PrivateRoute>
            <InterviewPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/interview/:id/result"
        element={
          <PrivateRoute>
            <InterviewResultPage />
          </PrivateRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
