import PropTypes from 'prop-types'

/**
 * 현재 질문 진행률을 표시합니다.
 */
function ProgressBar({ current, total }) {
  const safeTotal = Math.max(total, 1)
  const progress = Math.min((current / safeTotal) * 100, 100)

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-sm text-gray-600">
        <span>진행률</span>
        <span>
          {current}/{total}
        </span>
      </div>
      <div className="h-2 w-full rounded-full bg-gray-200">
        <div className="h-2 rounded-full bg-primary transition-all" style={{ width: `${progress}%` }} />
      </div>
    </div>
  )
}

ProgressBar.propTypes = {
  current: PropTypes.number.isRequired,
  total: PropTypes.number.isRequired,
}

export default ProgressBar
