import client from './client'
import type { ApiResponse, PaymentResponse, CreatePaymentRequest } from '../types'

export const paymentApi = {
  create: (data: CreatePaymentRequest) =>
    client.post<ApiResponse<PaymentResponse>>('/api/payments', data),

  getById: (id: number) =>
    client.get<ApiResponse<PaymentResponse>>(`/api/payments/${id}`),

  complete: (id: number) =>
    client.patch<ApiResponse<PaymentResponse>>(`/api/payments/${id}/complete`),

  refund: (id: number) =>
    client.patch<ApiResponse<PaymentResponse>>(`/api/payments/${id}/refund`),
}
