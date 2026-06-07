import PropTypes from 'prop-types'
import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { logout as logoutApi, refreshToken as refreshTokenApi } from '../api/auth.js'
import { clearTokens, getRefreshToken, getStoredUser, setStoredUser, setTokens } from '../utils/token.js'
import { unwrapApiData } from '../utils/unwrapApi.js'
import { notifyToast } from '../utils/toastNotify.js'

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
  } catch {
    notifyToast('로그인 정보를 처리하지 못했습니다.', 'error')
    return null
  }
}

/**
 * 인증 상태와 로그인/로그아웃 액션을 제공합니다.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredUser())

  const updateUser = useCallback((partial) => {
    setUser((prev) => {
      const base = prev ?? { email: '', planType: 'FREE' }
      const next = { ...base, ...partial }
      setStoredUser(next)
      return next
    })
  }, [])

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

  const refreshSession = useCallback(async () => {
    const token = getRefreshToken()
    if (!token) {
      return false
    }
    try {
      const response = await refreshTokenApi(token)
      const tokens = unwrapApiData(response)
      if (!tokens?.accessToken) {
        return false
      }
      setTokens(tokens.accessToken, tokens.refreshToken ?? token)
      const payload = parseJwtPayload(tokens.accessToken) || {}
      updateUser({
        email: payload.email ?? user?.email ?? '',
        planType: payload.planType ?? 'FREE',
      })
      return true
    } catch {
      return false
    }
  }, [updateUser, user?.email])

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        await logoutApi(refreshToken)
      }
    } catch {
      /* axios 인터셉터에서 이미 토스트 처리 */
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
      updateUser,
      refreshSession,
    }),
    [user, updateUser, refreshSession],
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
