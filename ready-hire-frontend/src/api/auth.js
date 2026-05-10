import axios from './axios.js'

export const refreshToken = (token) => axios.post('/api/auth/refresh', { refreshToken: token })

export const logout = (token) => axios.post('/api/auth/logout', { refreshToken: token })
