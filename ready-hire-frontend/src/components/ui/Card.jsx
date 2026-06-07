import PropTypes from 'prop-types'
import { cn } from '../../utils/cn.js'

/**
 * 흰 배경의 둥근 카드 컨테이너입니다.
 * 반복되던 `rounded-2xl border border-gray-100 bg-white p-6 shadow-sm` 패턴을 대체합니다.
 */
function Card({ as: Component = 'div', className, children, ...props }) {
  return (
    <Component
      className={cn('rounded-2xl border border-gray-100 bg-white p-6 shadow-sm', className)}
      {...props}
    >
      {children}
    </Component>
  )
}

Card.propTypes = {
  as: PropTypes.elementType,
  className: PropTypes.string,
  children: PropTypes.node,
}

export default Card
