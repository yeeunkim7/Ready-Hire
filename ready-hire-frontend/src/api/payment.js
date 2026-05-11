import axios from './axios.js'
import { unwrapApiData } from '../utils/unwrapApi.js'

export const getSubscriptionStatus = async () => {
  const res = await axios.get('/api/payments/subscription')
  return unwrapApiData(res)
}

export const verifyPayment = async (data) => {
  const res = await axios.post('/api/payments/verify', data)
  return unwrapApiData(res)
}

export const cancelSubscription = async () => {
  const res = await axios.delete('/api/payments/subscription')
  return unwrapApiData(res)
}
