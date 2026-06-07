/**
 * 의존성 없는 className 결합 유틸 (clsx 스타일).
 * 문자열/배열/조건부 객체를 받아 truthy한 클래스만 합칩니다.
 * @param {...(string | number | false | null | undefined | Record<string, boolean> | Array<any>)} inputs
 * @returns {string}
 */
function toValue(mix) {
  if (typeof mix === 'string' || typeof mix === 'number') {
    return String(mix)
  }
  if (Array.isArray(mix)) {
    return mix.map(toValue).filter(Boolean).join(' ')
  }
  if (mix && typeof mix === 'object') {
    return Object.keys(mix)
      .filter((key) => mix[key])
      .join(' ')
  }
  return ''
}

export function cn(...inputs) {
  return inputs.map(toValue).filter(Boolean).join(' ').trim()
}
