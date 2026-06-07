import PropTypes from 'prop-types'
import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import { cn } from '../utils/cn.js'

const MAX_WIDTHS = {
  md: 'max-w-md',
  '2xl': 'max-w-2xl',
  '3xl': 'max-w-3xl',
  '5xl': 'max-w-5xl',
}

/**
 * 로그인 이후 화면 공통 레이아웃입니다.
 * - Navbar를 한 번만 렌더링
 * - 일관된 배경(bg-gray-50)과 페이지 패딩 제공
 * - max-width로 콘텐츠를 가운데 정렬
 * 페이지별 본문은 <Outlet />으로 주입됩니다.
 */
function AppLayout({ maxWidth = '5xl' }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className={cn('mx-auto w-full space-y-6 px-4 py-6', MAX_WIDTHS[maxWidth] ?? MAX_WIDTHS['5xl'])}>
        <Outlet />
      </main>
    </div>
  )
}

AppLayout.propTypes = {
  maxWidth: PropTypes.oneOf(['md', '2xl', '3xl', '5xl']),
}

export default AppLayout
