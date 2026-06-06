import axios from './axios.js'
import { unwrapApiData } from '../utils/unwrapApi.js'

export const startInterview = async (data) => {
  const res = await axios.post('/api/interviews', data)
  return unwrapApiData(res)
}

export const submitAnswer = async (interviewId, data) => {
  const res = await axios.post(`/api/interviews/${interviewId}/answers`, data)
  return unwrapApiData(res)
}

export const completeInterview = async (interviewId) => {
  const res = await axios.post(`/api/interviews/${interviewId}/complete`)
  return unwrapApiData(res)
}

export const getInterviewDetail = async (interviewId) => {
  const res = await axios.get(`/api/interviews/${interviewId}`)
  return unwrapApiData(res)
}

export const getInterviewHistory = async () => {
  const res = await axios.get('/api/interviews')
  return unwrapApiData(res)
}

export const parsePdf = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const res = await axios.post('/api/interviews/parse-pdf', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return unwrapApiData(res)
}
