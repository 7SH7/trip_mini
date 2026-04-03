import client from './client'
import type { ApiResponse, BookingResponse, CreateBookingRequest } from '../types'

export const bookingApi = {
  create: (data: CreateBookingRequest) =>
    client.post<ApiResponse<BookingResponse>>('/api/bookings', data),

  getById: (id: number) =>
    client.get<ApiResponse<BookingResponse>>(`/api/bookings/${id}`),

  getMyBookings: () =>
    client.get<ApiResponse<BookingResponse[]>>('/api/bookings/my'),

  confirm: (id: number) =>
    client.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/confirm`),

  cancel: (id: number) =>
    client.patch<ApiResponse<BookingResponse>>(`/api/bookings/${id}/cancel`),
}
