import axios from 'axios'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '../utils/token.js'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

api.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request interceptor error:', error)
    return Promise.reject(error)
  },
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (!originalRequest) return Promise.reject(error)

    const isUnauthorized = error.response?.status === 401
    const isRefreshRoute = originalRequest.url?.includes('/api/auth/refresh')

    if (!isUnauthorized || isRefreshRoute || originalRequest._retry) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    try {
      const refreshToken = getRefreshToken()
      if (!refreshToken) {
        throw new Error('No refresh token available')
      }

      const response = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh`,
        { refreshToken },
      )
      const newAccessToken = response.data?.accessToken
      const newRefreshToken = response.data?.refreshToken ?? refreshToken

      if (!newAccessToken) {
        throw new Error('Refresh response missing accessToken')
      }

      setTokens(newAccessToken, newRefreshToken)
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      console.error('Token refresh failed:', refreshError)
      clearTokens()
      window.location.href = '/login'
      return Promise.reject(refreshError)
    }
  },
)

export default api
