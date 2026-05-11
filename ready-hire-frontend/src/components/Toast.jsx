import PropTypes from 'prop-types'
import { createPortal } from 'react-dom'

const typeStyles = {
  error: 'bg-red-50 text-red-800 border-red-200',
  success: 'bg-emerald-50 text-emerald-800 border-emerald-200',
  info: 'bg-blue-50 text-blue-800 border-blue-200',
}

/**
 * 우측 하단 고정 토스트 목록 (portal).
 * @param {{ id: string, message: string, type: 'error' | 'success' | 'info' }[]} props.toasts
 */
function Toast({ toasts }) {
  if (typeof document === 'undefined') return null

  return createPortal(
    <div className="pointer-events-none fixed bottom-4 right-4 z-[100] flex max-w-sm flex-col gap-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role="status"
          className={`pointer-events-auto animate-[toastIn_0.3s_ease-out] rounded-xl border px-4 py-3 text-sm shadow-lg ${typeStyles[toast.type] ?? typeStyles.info}`}
        >
          {toast.message}
        </div>
      ))}
    </div>,
    document.body,
  )
}

Toast.propTypes = {
  toasts: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      message: PropTypes.string.isRequired,
      type: PropTypes.oneOf(['error', 'success', 'info']).isRequired,
    }),
  ).isRequired,
}

export default Toast
