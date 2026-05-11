import { notifyToast } from './toastNotify.js'

/**
 * 백엔드 ApiResponse({ success, message, data })에서 data를 꺼냅니다.
 * @template T
 * @param {import('axios').AxiosResponse} response
 * @returns {T}
 */
export function unwrapApiData(response) {
  const body = response?.data
  if (!body || typeof body !== 'object') {
    return /** @type {T} */ (body)
  }
  if (body.success === false) {
    const msg = body.message || '요청에 실패했습니다.'
    notifyToast(msg, 'error')
    const err = new Error(msg)
    err.response = response
    throw err
  }
  return /** @type {T} */ (body.data)
}
