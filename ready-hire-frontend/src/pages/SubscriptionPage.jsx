import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { cancelSubscription, getSubscriptionStatus, verifyPayment } from '../api/payment.js'
import ConfirmModal from '../components/ConfirmModal.jsx'
import Navbar from '../components/Navbar.jsx'
import PlanBadge from '../components/PlanBadge.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

/**
 * 구독 상태 조회, PRO 결제(포트원), 구독 해지를 담당하는 페이지입니다.
 */
function SubscriptionPage() {
  const navigate = useNavigate()
  const { user, updateUser } = useAuth()
  const { showToast } = useToast()
  const [loading, setLoading] = useState(true)
  const [subscription, setSubscription] = useState(null)
  const [cancelOpen, setCancelOpen] = useState(false)
  const [paying, setPaying] = useState(false)

  const loadSubscription = useCallback(async () => {
    try {
      setLoading(true)
      const data = await getSubscriptionStatus()
      setSubscription(data)
    } catch {
      /* axios / unwrap 에서 토스트 처리 */
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadSubscription()
  }, [loadSubscription])

  const isPro = String(subscription?.planType ?? user?.planType ?? 'FREE').toUpperCase() === 'PRO'

  const handlePay = () => {
    try {
      const { IMP } = window
      if (!IMP) {
        showToast('결제 모듈을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.', 'error')
        return
      }

      const channelKey = import.meta.env.VITE_PORTONE_CHANNEL_KEY
      if (!channelKey) {
        showToast('결제 채널 키가 설정되지 않았습니다.', 'error')
        return
      }

      IMP.init(channelKey)
      setPaying(true)

      IMP.request_pay(
        {
          pg: 'tosspayments',
          pay_method: 'card',
          merchant_uid: `order_${Date.now()}`,
          name: 'Ready-Hire PRO 월간 구독',
          amount: 9900,
          buyer_email: user?.email ?? '',
        },
        async (rsp) => {
          try {
            if (!rsp?.success) {
              showToast(rsp?.error_msg || '결제가 취소되었습니다.', 'error')
              return
            }
            const data = await verifyPayment({
              paymentId: rsp.imp_uid,
              orderName: 'Ready-Hire PRO 월간 구독',
              amount: 9900,
            })
            updateUser({ planType: data?.planType ?? 'PRO' })
            showToast('PRO 구독이 활성화되었습니다.', 'success')
            await loadSubscription()
          } catch {
            /* verify 실패는 axios/unwrap 토스트 */
          } finally {
            setPaying(false)
          }
        },
      )
    } catch (error) {
      setPaying(false)
      showToast(error?.message || '결제 요청에 실패했습니다.', 'error')
    }
  }

  const handleCancelConfirm = async () => {
    try {
      await cancelSubscription()
      updateUser({ planType: 'FREE' })
      showToast('구독이 해지되었습니다.', 'success')
      setCancelOpen(false)
      await loadSubscription()
    } catch {
      /* 토스트는 인터셉터/unwrap */
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="mx-auto max-w-lg space-y-6 px-4 py-6">
        {loading ? (
          <p className="text-sm text-gray-500">구독 정보를 불러오는 중...</p>
        ) : !isPro ? (
          <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h1 className="text-xl font-bold">구독</h1>
              <PlanBadge planType="FREE" />
            </div>
            <p className="mt-2 text-sm text-gray-600">현재 플랜: FREE</p>

            <div className="mt-6 rounded-2xl border border-gray-100 bg-gray-50 p-4">
              <h2 className="font-semibold text-gray-900">PRO 플랜 혜택</h2>
              <ul className="mt-3 space-y-2 text-sm text-gray-700">
                <li>✅ 무제한 면접</li>
                <li>✅ 상세 AI 피드백 (잘한점/개선점/모범답안)</li>
                <li>✅ 전체 히스토리</li>
              </ul>
            </div>

            <button
              type="button"
              disabled={paying}
              onClick={handlePay}
              className="mt-6 w-full rounded-xl bg-primary px-6 py-3 font-medium text-white disabled:cursor-not-allowed disabled:bg-indigo-300"
            >
              {paying ? '결제 처리 중...' : '월 9,900원 — PRO로 업그레이드'}
            </button>
            <button type="button" onClick={() => navigate('/dashboard')} className="mt-3 w-full text-sm text-gray-500 underline">
              대시보드로 돌아가기
            </button>
          </section>
        ) : (
          <section className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h1 className="text-xl font-bold">구독</h1>
              <PlanBadge planType="PRO" />
            </div>
            <p className="mt-2 text-sm text-gray-600">현재 플랜: PRO</p>
            <p className="mt-2 text-sm text-gray-700">
              구독 만료일:{' '}
              {subscription?.expiresAt
                ? new Date(subscription.expiresAt).toLocaleString()
                : '정보 없음'}
            </p>

            <div className="mt-6">
              <h2 className="font-semibold text-gray-900">최근 결제 내역</h2>
              {subscription?.recentPayments?.length ? (
                <ul className="mt-3 space-y-2 text-sm">
                  {subscription.recentPayments.map((p) => (
                    <li key={p.portonePaymentId} className="rounded-xl border border-gray-100 p-3">
                      <p className="font-medium">{p.amount?.toLocaleString?.() ?? p.amount}원</p>
                      <p className="text-gray-500">{p.status}</p>
                      <p className="text-xs text-gray-400">{p.paidAt ? new Date(p.paidAt).toLocaleString() : ''}</p>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="mt-2 text-sm text-gray-500">최근 결제 내역이 없습니다.</p>
              )}
            </div>

            <button
              type="button"
              onClick={() => setCancelOpen(true)}
              className="mt-6 w-full rounded-xl border border-red-200 bg-red-50 px-6 py-3 text-sm font-medium text-red-700"
            >
              구독 해지
            </button>
          </section>
        )}
      </main>

      <ConfirmModal
        isOpen={cancelOpen}
        title="구독을 해지할까요?"
        message="해지 후에는 PRO 혜택을 사용할 수 없습니다. 계속하시겠습니까?"
        confirmText="해지하기"
        cancelText="닫기"
        isDanger
        onCancel={() => setCancelOpen(false)}
        onConfirm={handleCancelConfirm}
      />
    </div>
  )
}

export default SubscriptionPage
