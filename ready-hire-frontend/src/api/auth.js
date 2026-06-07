import axios from './axios.js'
import { unwrapApiData } from '../utils/unwrapApi.js'

export const signup = async (data) => {
  const res = await axios.post('/api/auth/signup', data)
  return unwrapApiData(res)
}

export const login = async (data) => {
  const res = await axios.post('/api/auth/login', data)
  return unwrapApiData(res)
}

export const refreshToken = (token) => axios.post('/api/auth/refresh', { refreshToken: token })

export const logout = (token) => axios.post('/api/auth/logout', { refreshToken: token })

