import PropTypes from 'prop-types'
import { createContext, useContext, useMemo, useState } from 'react'
import { logout as logoutApi } from '../api/auth.js'
import { clearTokens, getStoredUser, setStoredUser, setTokens } from '../utils/token.js'

const AuthContext = createContext(null)

const parseJwtPayload = (token) => {
  try {
    const payload = token.split('.')[1]
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = decodeURIComponent(
      atob(normalized)
        .split('')
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(''),
    )
    return JSON.parse(decoded)
  } catch (error) {
    console.error('Failed to parse JWT payload:', error)
    return null
  }
}

/**
 * 인증 상태와 로그인/로그아웃 액션을 제공합니다.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredUser())

  const login = (accessToken, refreshToken) => {
    setTokens(accessToken, refreshToken)
    const payload = parseJwtPayload(accessToken) || {}
    const normalizedUser = {
      email: payload.email ?? 'unknown@ready-hire.dev',
      planType: payload.planType ?? 'FREE',
    }
    setStoredUser(normalizedUser)
    setUser(normalizedUser)
  }

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        await logoutApi(refreshToken)
      }
    } catch (error) {
      console.error('Logout API failed:', error)
    } finally {
      clearTokens()
      setUser(null)
    }
  }

  const value = useMemo(
    () => ({
      user,
      isLoggedIn: Boolean(user),
      login,
      logout,
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

AuthProvider.propTypes = {
  children: PropTypes.node.isRequired,
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
