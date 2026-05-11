const noop = () => {}

/** @type {(message: string, type?: 'error' | 'success' | 'info') => void} */
let sink = noop

export function registerToastSink(fn) {
  sink = typeof fn === 'function' ? fn : noop
}

export function unregisterToastSink() {
  sink = noop
}

/**
 * axios 등 React 바깥에서도 호출 가능한 토스트 알림입니다.
 * @param {string} message
 * @param {'error' | 'success' | 'info'} [type]
 */
export function notifyToast(message, type = 'error') {
  try {
    sink(String(message || '요청에 실패했습니다.'), type)
  } catch {
    /* ignore */
  }
}
