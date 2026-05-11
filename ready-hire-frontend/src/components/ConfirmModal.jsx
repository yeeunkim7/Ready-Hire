import PropTypes from 'prop-types'
import { useEffect } from 'react'
import { createPortal } from 'react-dom'

/**
 * 확인/취소가 있는 모달 다이얼로그 (body portal, ESC로 닫기).
 */
function ConfirmModal({
  isOpen,
  title,
  message,
  onConfirm,
  onCancel,
  confirmText = '확인',
  cancelText = '취소',
  isDanger = false,
}) {
  useEffect(() => {
    if (!isOpen) return undefined
    const onKey = (e) => {
      if (e.key === 'Escape') onCancel()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [isOpen, onCancel])

  if (!isOpen || typeof document === 'undefined') return null

  return createPortal(
    <div className="fixed inset-0 z-[110] flex items-center justify-center bg-black/50 px-4">
      <div
        role="dialog"
        aria-modal="true"
        className="w-full max-w-md rounded-2xl border border-gray-100 bg-white p-6 shadow-xl"
      >
        <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
        <p className="mt-2 text-sm text-gray-600">{message}</p>
        <div className="mt-6 flex justify-end gap-2">
          <button type="button" onClick={onCancel} className="rounded-xl border border-gray-200 px-4 py-2 text-sm">
            {cancelText}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className={`rounded-xl px-4 py-2 text-sm font-medium text-white ${
              isDanger ? 'bg-red-600 hover:bg-red-700' : 'bg-primary hover:bg-indigo-700'
            }`}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>,
    document.body,
  )
}

ConfirmModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  title: PropTypes.string.isRequired,
  message: PropTypes.string.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  confirmText: PropTypes.string,
  cancelText: PropTypes.string,
  isDanger: PropTypes.bool,
}

export default ConfirmModal
