import PropTypes from 'prop-types'
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import Toast from '../components/Toast.jsx'
import { registerToastSink, unregisterToastSink } from '../utils/toastNotify.js'

const ToastContext = createContext(null)

/**
 * 앱 전역 토스트 알림을 제공합니다.
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const showToast = useCallback((message, type = 'info') => {
    const id = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
    setToasts((prev) => [...prev, { id, message: String(message || ''), type }])
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 3000)
  }, [])

  useEffect(() => {
    registerToastSink(showToast)
    return () => unregisterToastSink()
  }, [showToast])

  const value = useMemo(() => ({ showToast }), [showToast])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Toast toasts={toasts} />
    </ToastContext.Provider>
  )
}

ToastProvider.propTypes = {
  children: PropTypes.node.isRequired,
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) {
    throw new Error('useToast must be used within ToastProvider')
  }
  return ctx
}
