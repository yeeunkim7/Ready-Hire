import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import { SESSION_MODES, SESSION_MODE_LABELS } from '../constants/interviewSession.js'

const MODE_OPTIONS = [
  {
    id: SESSION_MODES.PRACTICE,
    title: SESSION_MODE_LABELS[SESSION_MODES.PRACTICE],
    description: '시간 제한 없이 천천히 답변을 준비할 수 있어요.',
    points: ['질문당 시간 제한 없음', '부담 없이 반복 연습', '답변 수정에 여유'],
  },
  {
    id: SESSION_MODES.EXAM,
    title: SESSION_MODE_LABELS[SESSION_MODES.EXAM],
    description: '실제 면접처럼 질문당 1분 30초 안에 답변해야 해요.',
    points: ['질문당 1분 30초 제한', '시간 초과 시 자동 제출', '실전 압박감 경험'],
  },
]

function InterviewModeSelectPage() {
  const navigate = useNavigate()
  const [sessionMode, setSessionMode] = useState(SESSION_MODES.PRACTICE)

  const handleNext = () => {
    navigate('/interview/setup', { state: { sessionMode } })
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-2xl space-y-6 px-4 py-6">
        <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
          <h1 className="text-xl font-bold">면접 모드 선택</h1>
          <p className="mt-2 text-sm text-gray-500">연습모드와 실전모드 중 하나를 선택한 뒤 면접 설정으로 이동합니다.</p>

          <div className="mt-6 grid gap-4 sm:grid-cols-2">
            {MODE_OPTIONS.map((option) => {
              const active = sessionMode === option.id
              return (
                <button
                  key={option.id}
                  type="button"
                  onClick={() => setSessionMode(option.id)}
                  className={`rounded-2xl border p-5 text-left transition ${
                    active
                      ? 'border-primary bg-indigo-50 ring-2 ring-primary'
                      : 'border-gray-200 bg-white hover:border-gray-300'
                  }`}
                >
                  <p className="text-lg font-semibold text-gray-900">{option.title}</p>
                  <p className="mt-2 text-sm text-gray-600">{option.description}</p>
                  <ul className="mt-3 space-y-1 text-sm text-gray-500">
                    {option.points.map((point) => (
                      <li key={point}>• {point}</li>
                    ))}
                  </ul>
                </button>
              )
            })}
          </div>

          <button
            type="button"
            onClick={handleNext}
            className="mt-6 w-full rounded-xl bg-primary px-6 py-3 font-medium text-white"
          >
            다음 — 면접 설정
          </button>
        </section>
      </main>
    </div>
  )
}

export default InterviewModeSelectPage
