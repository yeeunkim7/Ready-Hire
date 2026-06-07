import { forwardRef } from 'react'
import PropTypes from 'prop-types'
import { cn } from '../../utils/cn.js'

/**
 * 표준 텍스트 입력 프리미티브입니다.
 * 반복되던 `w-full rounded-xl border border-gray-200 px-3 py-2` 패턴을 대체합니다.
 */
const Input = forwardRef(function Input({ className, ...props }, ref) {
  return (
    <input
      ref={ref}
      className={cn(
        'w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-primary focus:outline-none',
        className,
      )}
      {...props}
    />
  )
})

Input.propTypes = {
  className: PropTypes.string,
}

export default Input
