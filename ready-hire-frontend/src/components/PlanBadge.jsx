import PropTypes from 'prop-types'

/**
 * FREE / PRO 플랜 상태를 보여주는 배지입니다.
 */
function PlanBadge({ planType }) {
  const normalized = String(planType ?? 'FREE').toUpperCase()
  const isPro = normalized === 'PRO'
  return (
    <span
      className={`rounded-full px-3 py-1 text-xs font-semibold ${
        isPro ? 'bg-purple-100 text-purple-700' : 'bg-gray-200 text-gray-700'
      }`}
    >
      {isPro ? 'PRO' : 'FREE'}
    </span>
  )
}

PlanBadge.propTypes = {
  planType: PropTypes.string,
}

export default PlanBadge
