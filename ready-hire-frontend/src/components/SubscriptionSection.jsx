import { useCallback, useEffect, useState } from 'react'
import { cancelSubscription, getSubscriptionStatus, verifyPayment } from '../api/payment.js'
import ConfirmModal from './ConfirmModal.jsx'
import PlanBadge from './PlanBadge.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import { useToast } from '../contexts/ToastContext.jsx'

/**
 * 마이페이지 내 구독 관리 섹션입니다.
 */
function SubscriptionSection() {
  const { user, refreshSession } = useAuth()
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
      /* axios / unwrap */
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
            await verifyPayment({
              paymentId: rsp.imp_uid,
              orderName: 'Ready-Hire PRO 월간 구독',
              amount: 9900,
            })
            await refreshSession()
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
      await refreshSession()
      showToast('구독이 해지되었습니다.', 'success')
      setCancelOpen(false)
      await loadSubscription()
    } catch {
      /* 토스트는 인터셉터/unwrap */
    }
  }

  if (loading) {
    return (
      <section id="subscription" className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold">구독 관리</h2>
        <p className="mt-4 text-sm text-gray-500">구독 정보를 불러오는 중...</p>
      </section>
    )
  }

  return (
    <section id="subscription" className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">구독 관리</h2>
        <PlanBadge planType={isPro ? 'PRO' : 'FREE'} />
      </div>

      {!isPro ? (
        <>
          <p className="mt-2 text-sm text-gray-600">현재 플랜: FREE</p>
          <div className="mt-4 rounded-2xl border border-gray-100 bg-gray-50 p-4">
            <h3 className="font-semibold text-gray-900">PRO 플랜 혜택</h3>
            <ul className="mt-3 space-y-2 text-sm text-gray-700">
              <li>무제한 면접</li>
              <li>상세 AI 피드백 (잘한점/개선점/모범답안)</li>
              <li>PDF 맞춤 면접 · 전체 히스토리</li>
            </ul>
          </div>
          <button
            type="button"
            disabled={paying}
            onClick={handlePay}
            className="mt-4 w-full rounded-xl bg-primary px-6 py-3 font-medium text-white disabled:cursor-not-allowed disabled:bg-indigo-300"
          >
            {paying ? '결제 처리 중...' : '월 9,900원 — PRO로 업그레이드'}
          </button>
        </>
      ) : (
        <>
          <p className="mt-2 text-sm text-gray-600">현재 플랜: PRO</p>
          <p className="mt-2 text-sm text-gray-700">
            구독 만료일:{' '}
            {subscription?.expiresAt
              ? new Date(subscription.expiresAt).toLocaleString()
              : '정보 없음'}
          </p>

          <div className="mt-4">
            <h3 className="font-semibold text-gray-900">최근 결제 내역</h3>
            {subscription?.recentPayments?.length ? (
              <ul className="mt-3 space-y-2 text-sm">
                {subscription.recentPayments.map((p) => (
                  <li key={p.portonePaymentId} className="rounded-xl border border-gray-100 p-3">
                    <p className="font-medium">{p.amount?.toLocaleString?.() ?? p.amount}원</p>
                    <p className="text-gray-500">{p.status}</p>
                    <p className="text-xs text-gray-400">
                      {p.paidAt ? new Date(p.paidAt).toLocaleString() : ''}
                    </p>
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
            className="mt-4 w-full rounded-xl border border-red-200 bg-red-50 px-6 py-3 text-sm font-medium text-red-700"
          >
            구독 해지
          </button>
        </>
      )}

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
    </section>
  )
}

export default SubscriptionSection
