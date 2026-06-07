import axios from './axios.js'
import { unwrapApiData } from '../utils/unwrapApi.js'

export const getMyPage = async () => {
  const res = await axios.get('/api/users/me')
  return unwrapApiData(res)
}

export const updateMyProfile = async (data) => {
  const res = await axios.patch('/api/users/me', data)
  return unwrapApiData(res)
}

export const withdrawAccount = async (refreshToken) => {
  const res = await axios.delete('/api/users/me', { data: { refreshToken } })
  return unwrapApiData(res)
}

export const getMyInterviews = async (page = 0, size = 10) => {
  const res = await axios.get('/api/users/me/interviews', { params: { page, size } })
  return unwrapApiData(res)
}

export const getMyInterviewDetail = async (interviewId) => {
  const res = await axios.get(`/api/users/me/interviews/${interviewId}`)
  return unwrapApiData(res)
}
