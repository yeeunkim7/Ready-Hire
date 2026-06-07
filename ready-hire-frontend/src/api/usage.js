import axios from './axios.js'
import { unwrapApiData } from '../utils/unwrapApi.js'

export const getTodayUsage = async () => {
  const res = await axios.get('/api/usage/today')
  return unwrapApiData(res)
}
