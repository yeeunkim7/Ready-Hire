import PropTypes from 'prop-types'
import { cn } from '../../utils/cn.js'

const VARIANT_CLASSES = {
  primary: 'bg-primary text-white hover:bg-indigo-700 disabled:bg-indigo-300',
  secondary: 'bg-secondary text-white hover:bg-purple-700 disabled:opacity-60',
  outline: 'border border-gray-200 bg-white text-gray-800 hover:bg-gray-50',
  danger: 'bg-red-600 text-white hover:bg-red-700',
  dangerOutline: 'border border-red-200 bg-red-50 text-red-700 hover:bg-red-100',
  ghost: 'text-gray-600 underline hover:text-gray-800',
}

const SIZE_CLASSES = {
  sm: 'px-4 py-2 text-sm',
  md: 'px-6 py-3 text-sm',
}

/**
 * 앱 전역에서 재사용하는 버튼 프리미티브입니다.
 * 비즈니스 로직은 onClick 등으로 주입받기만 하고, 스타일만 표준화합니다.
 */
function Button({ variant = 'primary', size = 'md', type = 'button', className, children, ...props }) {
  return (
    <button
      type={type}
      className={cn(
        'rounded-xl font-medium transition disabled:cursor-not-allowed',
        VARIANT_CLASSES[variant] ?? VARIANT_CLASSES.primary,
        SIZE_CLASSES[size] ?? SIZE_CLASSES.md,
        className,
      )}
      {...props}
    >
      {children}
    </button>
  )
}

Button.propTypes = {
  variant: PropTypes.oneOf(['primary', 'secondary', 'outline', 'danger', 'dangerOutline', 'ghost']),
  size: PropTypes.oneOf(['sm', 'md']),
  type: PropTypes.string,
  className: PropTypes.string,
  children: PropTypes.node,
}

export default Button
