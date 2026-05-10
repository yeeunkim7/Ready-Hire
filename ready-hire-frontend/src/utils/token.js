const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY)

export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY)

export const getStoredUser = () => {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (error) {
    console.error('Failed to parse stored user:', error)
    return null
  }
}

export const setTokens = (access, refresh) => {
  if (access) localStorage.setItem(ACCESS_TOKEN_KEY, access)
  if (refresh) localStorage.setItem(REFRESH_TOKEN_KEY, refresh)
}

export const setStoredUser = (user) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const clearTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
