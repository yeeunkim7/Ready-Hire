import PropTypes from 'prop-types'

/**
 * 전체 페이지 또는 섹션에서 재사용하는 로딩 스피너입니다.
 */
function LoadingSpinner({ fullScreen = false, message = '로딩 중...' }) {
  return (
    <div className={fullScreen ? 'flex min-h-screen items-center justify-center' : 'flex items-center justify-center py-8'}>
      <div className="flex flex-col items-center gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-primary" />
        <p className="text-sm text-gray-500">{message}</p>
      </div>
    </div>
  )
}

LoadingSpinner.propTypes = {
  fullScreen: PropTypes.bool,
  message: PropTypes.string,
}

export default LoadingSpinner
