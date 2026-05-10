import axios from './axios.js'

export const startInterview = (data) => axios.post('/api/interviews', data)

export const submitAnswer = (interviewId, data) =>
  axios.post(`/api/interviews/${interviewId}/answers`, data)

export const completeInterview = (interviewId) => axios.post(`/api/interviews/${interviewId}/complete`)

export const getInterviewDetail = (interviewId) => axios.get(`/api/interviews/${interviewId}`)

export const getInterviewHistory = () => axios.get('/api/interviews')
