import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { startInterview } from '../api/interview.js'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import Navbar from '../components/Navbar.jsx'

const TECH_STACKS = ['Java', 'Spring', 'Python', 'React', 'Vue', 'Node.js', 'Docker', 'K8s']

/**
 * 면접 시작 전에 직무/기술스택/경력을 입력받는 페이지입니다.
 */
function InterviewSetupPage() {
  const navigate = useNavigate()
  const [jobRole, setJobRole] = useState('백엔드')
  const [careerLevel, setCareerLevel] = useState('신입')
  const [techStacks, setTechStacks] = useState(['Java', 'Spring'])
  const [loading, setLoading] = useState(false)

  const toggleStack = (stack) => {
    setTechStacks((prev) => (prev.includes(stack) ? prev.filter((item) => item !== stack) : [...prev, stack]))
  }

  const handleStart = async () => {
    try {
      setLoading(true)
      const payload = {
        jobRole,
        techStack: techStacks,
        experienceLevel: careerLevel,
      }
      const data = await startInterview(payload)
      const interviewId = data?.interviewId
      if (!interviewId) {
        throw new Error('Interview id missing in response')
      }
      const questions = data?.questions ?? []
      sessionStorage.setItem(`interview_questions_${interviewId}`, JSON.stringify(questions))
      navigate(`/interview/${interviewId}`, { state: { questions } })
    } catch {
      /* axios / unwrap 토스트 */
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-md space-y-6 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <h1 className="text-xl font-bold">면접 설정</h1>
          <div className="mt-6 space-y-6">
            <div>
              <p className="mb-2 text-sm font-medium">1. 직무 선택</p>
              <select
                className="w-full rounded-xl border border-gray-200 px-3 py-2"
                value={jobRole}
                onChange={(event) => setJobRole(event.target.value)}
              >
                <option>백엔드</option>
                <option>프론트엔드</option>
                <option>풀스택</option>
                <option>데이터</option>
                <option>DevOps</option>
              </select>
            </div>

            <div>
              <p className="mb-2 text-sm font-medium">2. 기술스택 선택</p>
              <div className="flex flex-wrap gap-2">
                {TECH_STACKS.map((stack) => {
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
              <p className="mb-2 text-sm font-medium">3. 경력 선택</p>
              <div className="space-y-2">
                {['신입', '1~3년', '3~5년', '5년 이상'].map((career) => (
                  <label key={career} className="flex items-center gap-2 text-sm">
                    <input type="radio" name="career" checked={careerLevel === career} onChange={() => setCareerLevel(career)} />
                    {career}
                  </label>
                ))}
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={handleStart}
            disabled={loading}
            className="mt-6 w-full rounded-xl bg-primary px-6 py-3 font-medium text-white disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {loading ? '면접 생성 중...' : '면접 시작'}
          </button>
        </section>
        {loading && <LoadingSpinner message="질문을 생성하고 있어요..." />}
      </main>
    </div>
  )
}

export default InterviewSetupPage
