import { useEffect, useRef, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { SESSION_MODE_LABELS, SESSION_MODES, sessionModeStorageKey } from '../constants/interviewSession.js'
import { parsePdf, startInterview } from '../api/interview.js'
import ConfirmModal from '../components/ConfirmModal.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import Navbar from '../components/Navbar.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

const PRO_PDF_BANNER_KEY = 'pro_pdf_banner_dismissed'

const INTERVIEW_MODES = {
  STANDARD: 'STANDARD',
  JOB_POSTING: 'JOB_POSTING',
  PORTFOLIO: 'PORTFOLIO',
}

const MODE_TABS = [
  { id: INTERVIEW_MODES.STANDARD, label: '기본 면접' },
  { id: INTERVIEW_MODES.JOB_POSTING, label: '채용공고 맞춤 면접' },
  { id: INTERVIEW_MODES.PORTFOLIO, label: '포트폴리오 맞춤 면접' },
]

const CATEGORY_ORDER = [
  '개발/IT',
  '경영/기획',
  '마케팅/미디어',
  '회계/재무',
  '영업/고객',
  '디자인',
  '기타',
]

const JOB_CATEGORIES = {
  '개발/IT': {
    roles: [
      '백엔드 개발자',
      '프론트엔드 개발자',
      '풀스택 개발자',
      '데이터 엔지니어',
      'DevOps/인프라',
      'AI/ML 엔지니어',
      'iOS/Android',
    ],
    skills: ['Java', 'Spring', 'Python', 'React', 'Vue', 'Node.js', 'Docker', 'K8s', 'AWS'],
  },
  '경영/기획': {
    roles: ['경영기획', '사업개발', 'PM/PO', '컨설턴트', '전략기획'],
    skills: ['Excel', 'PowerPoint', 'SQL', 'Jira', 'Notion', 'Figma'],
  },
  '마케팅/미디어': {
    roles: ['디지털 마케터', '콘텐츠 마케터', 'SNS 마케터', 'PR/홍보', '영상 PD', '기자'],
    skills: ['GA4', 'Meta Ads', 'Google Ads', 'Figma', 'Canva', 'Photoshop'],
  },
  '회계/재무': {
    roles: ['회계사', '재무분석', '세무사', 'IR', '원가관리'],
    skills: ['ERP', 'SAP', 'Excel', '재무모델링', 'IFRS'],
  },
  '영업/고객': {
    roles: ['영업', 'B2B 영업', '고객성공(CS)', '파트너십'],
    skills: ['CRM', 'Salesforce', 'HubSpot', '협상', '프레젠테이션'],
  },
  디자인: {
    roles: ['UI/UX 디자이너', '그래픽 디자이너', '브랜드 디자이너', '영상 편집'],
    skills: ['Figma', 'Photoshop', 'Illustrator', 'After Effects', 'Premiere'],
  },
  기타: {
    roles: [],
    skills: ['MS Office', '협업툴', '커뮤니케이션', '문제해결', '프로젝트 관리'],
  },
}

const CAREER_LEVELS = ['신입', '1~3년', '3~5년', '5년 이상']

const PDF_MODE_CONFIG = {
  [INTERVIEW_MODES.JOB_POSTING]: {
    title: '채용공고 PDF 업로드',
    placeholder: 'PDF를 드래그하거나 클릭해서 업로드',
    successPrefix: '채용공고 분석 완료',
    missingMessage: '채용공고 PDF를 업로드해 주세요.',
  },
  [INTERVIEW_MODES.PORTFOLIO]: {
    title: '포트폴리오 PDF 업로드',
    placeholder: '이력서 또는 포트폴리오 PDF를 업로드하세요',
    successPrefix: '포트폴리오 분석 완료',
    missingMessage: '포트폴리오 PDF를 업로드해 주세요.',
  },
}

/**
 * 면접 시작 전에 직무/기술스택/경력 또는 PDF 기반 맞춤 설정을 받는 페이지입니다.
 */
function InterviewSetupPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()
  const sessionMode = location.state?.sessionMode ?? SESSION_MODES.PRACTICE
  const { showToast } = useToast()
  const fileInputRef = useRef(null)

  const isPro = String(user?.planType ?? 'FREE').toUpperCase() === 'PRO'
  const isPdfModeLocked = (mode) =>
    !isPro && (mode === INTERVIEW_MODES.JOB_POSTING || mode === INTERVIEW_MODES.PORTFOLIO)

  const [interviewMode, setInterviewMode] = useState(INTERVIEW_MODES.STANDARD)
  const [showProModal, setShowProModal] = useState(false)
  const [proBannerDismissed, setProBannerDismissed] = useState(
    () => sessionStorage.getItem(PRO_PDF_BANNER_KEY) === 'true',
  )
  const [category, setCategory] = useState('개발/IT')
  const [jobRole, setJobRole] = useState(JOB_CATEGORIES['개발/IT'].roles[0])
  const [customJobRole, setCustomJobRole] = useState('')
  const [careerLevel, setCareerLevel] = useState('신입')
  const [techStacks, setTechStacks] = useState(['Java', 'Spring'])
  const [jobPostingText, setJobPostingText] = useState('')
  const [portfolioText, setPortfolioText] = useState('')
  const [pdfCharCount, setPdfCharCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const [pdfLoading, setPdfLoading] = useState(false)
  const [dragActive, setDragActive] = useState(false)

  useEffect(() => {
    if (!location.state?.sessionMode) {
      navigate('/interview/mode', { replace: true })
    }
  }, [location.state?.sessionMode, navigate])

  const currentCategory = JOB_CATEGORIES[category]
  const resolvedJobRole = category === '기타' ? customJobRole.trim() : jobRole
  const availableSkills = currentCategory.skills
  const isPdfMode = interviewMode === INTERVIEW_MODES.JOB_POSTING || interviewMode === INTERVIEW_MODES.PORTFOLIO
  const pdfConfig = PDF_MODE_CONFIG[interviewMode]
  const pdfText = interviewMode === INTERVIEW_MODES.PORTFOLIO ? portfolioText : jobPostingText

  const handleCategoryChange = (nextCategory) => {
    setCategory(nextCategory)
    const nextConfig = JOB_CATEGORIES[nextCategory]
    if (nextCategory === '기타') {
      setCustomJobRole('')
      setTechStacks([])
      return
    }
    setJobRole(nextConfig.roles[0])
    setTechStacks(nextConfig.skills.slice(0, 2))
  }

  const handleModeChange = (nextMode) => {
    if (isPdfModeLocked(nextMode)) {
      setShowProModal(true)
      return
    }
    setInterviewMode(nextMode)
    setPdfCharCount(0)
    setDragActive(false)
  }

  const dismissProBanner = () => {
    sessionStorage.setItem(PRO_PDF_BANNER_KEY, 'true')
    setProBannerDismissed(true)
  }

  const toggleStack = (stack) => {
    setTechStacks((prev) => {
      if (prev.includes(stack)) {
        return prev.filter((item) => item !== stack)
      }
      if (prev.length >= 5) {
        showToast('기술스택은 최대 5개까지 선택할 수 있습니다.', 'error')
        return prev
      }
      return [...prev, stack]
    })
  }

  const uploadPdfFile = async (file) => {
    if (!file) return
    if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
      showToast('PDF 파일만 업로드할 수 있습니다.', 'error')
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      showToast('PDF 파일은 최대 10MB까지 업로드할 수 있습니다.', 'error')
      return
    }

    try {
      setPdfLoading(true)
      const data = await parsePdf(file)
      const text = data?.text ?? ''
      const charCount = data?.charCount ?? text.length

      if (interviewMode === INTERVIEW_MODES.PORTFOLIO) {
        setPortfolioText(text)
      } else {
        setJobPostingText(text)
      }
      setPdfCharCount(charCount)
      showToast(`${pdfConfig.successPrefix} (${charCount.toLocaleString()}자)`, 'success')
    } catch {
      if (interviewMode === INTERVIEW_MODES.PORTFOLIO) {
        setPortfolioText('')
      } else {
        setJobPostingText('')
      }
      setPdfCharCount(0)
    } finally {
      setPdfLoading(false)
    }
  }

  const handleFileChange = async (event) => {
    const file = event.target.files?.[0]
    await uploadPdfFile(file)
    event.target.value = ''
  }

  const handleDrop = async (event) => {
    event.preventDefault()
    setDragActive(false)
    const file = event.dataTransfer.files?.[0]
    await uploadPdfFile(file)
  }

  const handleStart = async () => {
    if (!resolvedJobRole) {
      showToast('직무를 선택하거나 입력해 주세요.', 'error')
      return
    }

    if (interviewMode === INTERVIEW_MODES.STANDARD && techStacks.length === 0) {
      showToast('기술스택을 1개 이상 선택해 주세요.', 'error')
      return
    }

    if (interviewMode === INTERVIEW_MODES.JOB_POSTING && !jobPostingText) {
      showToast(PDF_MODE_CONFIG[INTERVIEW_MODES.JOB_POSTING].missingMessage, 'error')
      return
    }

    if (interviewMode === INTERVIEW_MODES.PORTFOLIO && !portfolioText) {
      showToast(PDF_MODE_CONFIG[INTERVIEW_MODES.PORTFOLIO].missingMessage, 'error')
      return
    }

    try {
      setLoading(true)
      const payload = {
        jobRole: resolvedJobRole,
        techStack: techStacks,
        experienceLevel: careerLevel,
        interviewMode,
      }

      if (interviewMode === INTERVIEW_MODES.JOB_POSTING) {
        payload.jobPostingText = jobPostingText
      }
      if (interviewMode === INTERVIEW_MODES.PORTFOLIO) {
        payload.portfolioText = portfolioText
        payload.techStack = techStacks.length ? techStacks : []
      }

      const data = await startInterview(payload)
      const interviewId = data?.interviewId
      if (!interviewId) {
        throw new Error('Interview id missing in response')
      }
      const questions = data?.questions ?? []
      sessionStorage.setItem(`interview_questions_${interviewId}`, JSON.stringify(questions))
      sessionStorage.setItem(`interview_mode_${interviewId}`, interviewMode)
      sessionStorage.setItem(sessionModeStorageKey(interviewId), sessionMode)
      navigate(`/interview/${interviewId}`, { state: { questions, interviewMode, sessionMode } })
    } catch {
      /* axios / unwrap 토스트 */
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-2xl space-y-6 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h1 className="text-xl font-bold">면접 설정</h1>
            <div className="flex items-center gap-2">
              <span className="rounded-full bg-indigo-50 px-3 py-1 text-sm font-medium text-primary">
                {SESSION_MODE_LABELS[sessionMode] ?? SESSION_MODE_LABELS[SESSION_MODES.PRACTICE]}
              </span>
              <button
                type="button"
                onClick={() => navigate('/interview/mode', { state: { sessionMode } })}
                className="text-xs text-gray-500 underline"
              >
                모드 변경
              </button>
            </div>
          </div>

          <div className="mt-6">
            <p className="mb-2 text-sm font-medium">면접 모드</p>
            <div className="flex flex-wrap gap-2">
              {MODE_TABS.map((mode) => (
                <button
                  key={mode.id}
                  type="button"
                  onClick={() => handleModeChange(mode.id)}
                  className={`rounded-xl px-4 py-2 text-sm font-medium ${
                    interviewMode === mode.id ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'
                  }`}
                >
                  {isPdfModeLocked(mode.id) ? `🔒 ${mode.label}` : mode.label}
                </button>
              ))}
            </div>
          </div>

          {interviewMode === INTERVIEW_MODES.STANDARD && !isPro && !proBannerDismissed && (
            <div className="mt-4 flex items-start justify-between gap-3 rounded-xl border border-indigo-100 bg-indigo-50 px-4 py-3 text-sm text-indigo-900">
              <p>
                💡 PRO 플랜에서는 채용공고/포트폴리오 PDF를 업로드해서 맞춤형 면접을 볼 수 있어요!
              </p>
              <div className="flex shrink-0 items-center gap-2">
                <button
                  type="button"
                  onClick={() => navigate('/mypage#subscription')}
                  className="whitespace-nowrap text-sm font-semibold text-primary underline"
                >
                  PRO 알아보기
                </button>
                <button
                  type="button"
                  onClick={dismissProBanner}
                  className="text-gray-500 hover:text-gray-700"
                  aria-label="안내 닫기"
                >
                  ✕
                </button>
              </div>
            </div>
          )}

          {isPdfMode && pdfConfig && (
            <div className="mt-6">
              <p className="mb-2 text-sm font-medium">{pdfConfig.title}</p>
              <div
                role="button"
                tabIndex={0}
                onClick={() => fileInputRef.current?.click()}
                onKeyDown={(event) => event.key === 'Enter' && fileInputRef.current?.click()}
                onDragOver={(event) => {
                  event.preventDefault()
                  setDragActive(true)
                }}
                onDragLeave={() => setDragActive(false)}
                onDrop={handleDrop}
                className={`rounded-2xl border-2 border-dashed px-6 py-10 text-center transition ${
                  dragActive ? 'border-primary bg-indigo-50' : 'border-gray-200 bg-gray-50'
                }`}
              >
                <p className="text-sm font-medium text-gray-800">{pdfConfig.placeholder}</p>
                <p className="mt-1 text-xs text-gray-500">최대 10MB · application/pdf</p>
                {pdfLoading && <p className="mt-3 text-sm text-primary">PDF 분석 중...</p>}
                {!pdfLoading && pdfText && (
                  <p className="mt-3 text-sm font-medium text-green-600">
                    {pdfConfig.successPrefix} ({pdfCharCount.toLocaleString()}자)
                  </p>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="application/pdf,.pdf"
                className="hidden"
                onChange={handleFileChange}
              />
            </div>
          )}

          <div className="mt-6 space-y-6">
            <div>
              <p className="mb-2 text-sm font-medium">1. 직무 카테고리</p>
              <div className="flex flex-wrap gap-2">
                {CATEGORY_ORDER.map((item) => (
                  <button
                    key={item}
                    type="button"
                    onClick={() => handleCategoryChange(item)}
                    className={`rounded-full px-3 py-1 text-sm ${
                      category === item ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'
                    }`}
                  >
                    {item}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <p className="mb-2 text-sm font-medium">2. 직무 선택</p>
              {category === '기타' ? (
                <input
                  type="text"
                  value={customJobRole}
                  onChange={(event) => setCustomJobRole(event.target.value)}
                  placeholder="직무를 직접 입력하세요"
                  className="w-full rounded-xl border border-gray-200 px-3 py-2"
                />
              ) : (
                <div className="flex flex-wrap gap-2">
                  {currentCategory.roles.map((role) => (
                    <button
                      key={role}
                      type="button"
                      onClick={() => setJobRole(role)}
                      className={`rounded-full px-3 py-1 text-sm ${
                        jobRole === role ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'
                      }`}
                    >
                      {role}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div>
              <p className="mb-2 text-sm font-medium">
                3.{' '}
                {isPdfMode ? '역량/툴 선택 (선택)' : '기술스택/역량 선택'}
              </p>
              <div className="flex flex-wrap gap-2">
                {availableSkills.map((stack) => {
                  const active = techStacks.includes(stack)
                  return (
                    <button
                      key={stack}
                      type="button"
                      onClick={() => toggleStack(stack)}
                      className={`rounded-full px-3 py-1 text-sm ${
                        active ? 'bg-primary text-white' : 'bg-gray-100 text-gray-700'
                      }`}
                    >
                      {stack}
                    </button>
                  )
                })}
              </div>
            </div>

            <div>
              <p className="mb-2 text-sm font-medium">4. 경력 선택</p>
              <div className="space-y-2">
                {CAREER_LEVELS.map((career) => (
                  <label key={career} className="flex items-center gap-2 text-sm">
                    <input
                      type="radio"
                      name="career"
                      checked={careerLevel === career}
                      onChange={() => setCareerLevel(career)}
                    />
                    {career}
                  </label>
                ))}
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={handleStart}
            disabled={loading || pdfLoading}
            className="mt-6 w-full rounded-xl bg-primary px-6 py-3 font-medium text-white disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {loading ? '면접 생성 중...' : '면접 시작'}
          </button>
        </section>
        {loading && <LoadingSpinner message="질문을 생성하고 있어요..." />}
      </main>

      <ConfirmModal
        isOpen={showProModal}
        title="🔒 PRO 플랜 전용 기능"
        message="채용공고/포트폴리오 맞춤 면접은 PRO 플랜에서만 이용 가능합니다."
        confirmText="PRO 업그레이드 →"
        cancelText="닫기"
        onConfirm={() => {
          setShowProModal(false)
          navigate('/mypage#subscription')
        }}
        onCancel={() => setShowProModal(false)}
      />
    </div>
  )
}

export default InterviewSetupPage
