/**
 * 점수 구간에 맞는 Tailwind 텍스트 색상 클래스.
 * @param {number | string | null | undefined} score
 * @returns {string}
 */
export function getScoreColorClass(score) {
  const n = Number(score)
  if (Number.isNaN(n)) return 'text-gray-600'
  if (n >= 90) return 'text-emerald-600'
  if (n >= 70) return 'text-blue-600'
  if (n >= 50) return 'text-amber-600'
  return 'text-red-600'
}
