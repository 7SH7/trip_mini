import client from './client'
import type { ApiResponse, SubscriptionResponse, UseVideoCallResponse, CreditPurchaseResponse, PurchaseCreditsRequest } from '../types'

export const subscriptionApi = {
  getMy: () =>
    client.get<ApiResponse<SubscriptionResponse>>('/api/subscriptions/my'),

  useVideoCall: () =>
    client.post<ApiResponse<UseVideoCallResponse>>('/api/subscriptions/video-call/use'),

  purchaseCredits: (data: PurchaseCreditsRequest) =>
    client.post<ApiResponse<CreditPurchaseResponse>>('/api/subscriptions/credits/purchase', data),

  getPurchaseHistory: () =>
    client.get<ApiResponse<CreditPurchaseResponse[]>>('/api/subscriptions/credits/history'),
}
