import axios from 'axios'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '../utils/token.js'
import { notifyToast } from '../utils/toastNotify.js'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

function extractErrorMessage(error) {
  const data = error.response?.data
  if (data && typeof data === 'object' && typeof data.message === 'string') {
    return data.message
  }
  if (typeof error.message === 'string' && error.message) {
    return error.message
  }
  return '요청에 실패했습니다.'
}

api.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    notifyToast(extractErrorMessage(error), 'error')
    return Promise.reject(error)
  },
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (!originalRequest) {
      notifyToast(extractErrorMessage(error), 'error')
      return Promise.reject(error)
    }

    const isUnauthorized = error.response?.status === 401
    const isRefreshRoute = originalRequest.url?.includes('/api/auth/refresh')

    if (!isUnauthorized || isRefreshRoute || originalRequest._retry) {
      notifyToast(extractErrorMessage(error), 'error')
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      const refreshToken = getRefreshToken()
      if (!refreshToken) {
        throw new Error('세션이 만료되었습니다.')
      }

      const response = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
        { refreshToken },
      )
      const newAccessToken = response.data?.accessToken
      const newRefreshToken = response.data?.refreshToken ?? refreshToken

      if (!newAccessToken) {
        throw new Error('토큰 재발급 응답이 올바르지 않습니다.')
      }

      setTokens(newAccessToken, newRefreshToken)
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      notifyToast(extractErrorMessage(refreshError), 'error')
      clearTokens()
      window.location.href = '/login'
      return Promise.reject(refreshError)
    }
  },
)

export default api
