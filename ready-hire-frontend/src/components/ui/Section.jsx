import PropTypes from 'prop-types'
import Card from './Card.jsx'

/**
 * 제목/액션 헤더가 있는 콘텐츠 그룹 블록입니다.
 * 반복되던 `<section className="card"><h2>제목</h2>...</section>` 패턴을 표준화합니다.
 */
function Section({ title, action, children, className, ...props }) {
  return (
    <Card as="section" className={className} {...props}>
      {(title || action) && (
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          {title ? <h2 className="text-lg font-semibold text-gray-900">{title}</h2> : <span />}
          {action}
        </div>
      )}
      {children}
    </Card>
  )
}

Section.propTypes = {
  title: PropTypes.node,
  action: PropTypes.node,
  children: PropTypes.node,
  className: PropTypes.string,
}

export default Section
