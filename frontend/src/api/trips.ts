import client from './client'
import type {
  ApiResponse, TripResponse, CreateTripRequest,
  TripMemberResponse, InviteCodeResponse, TripScheduleResponse,
  TripPlaceResponse, TripExpenseResponse, ExpenseSummaryResponse,
  TripJoinRequestResponse
} from '../types'

export const tripApi = {
  create: (data: CreateTripRequest) =>
    client.post<ApiResponse<TripResponse>>('/api/trips', data),
  getById: (id: number) =>
    client.get<ApiResponse<TripResponse>>(`/api/trips/${id}`),
  getMyTrips: () =>
    client.get<ApiResponse<TripResponse[]>>('/api/trips/my'),

  // Members
  getMembers: (tripId: number) =>
    client.get<ApiResponse<TripMemberResponse[]>>(`/api/trips/${tripId}/members`),
  generateInviteCode: (tripId: number) =>
    client.post<ApiResponse<InviteCodeResponse>>(`/api/trips/${tripId}/members/invite`),
  joinByCode: (code: string) =>
    client.post<ApiResponse<TripJoinRequestResponse>>('/api/trips/join', { code }),
  getJoinRequests: (tripId: number) =>
    client.get<ApiResponse<TripJoinRequestResponse[]>>(`/api/trips/${tripId}/join-requests`),
  approveJoinRequest: (tripId: number, requestId: number) =>
    client.post<ApiResponse<TripJoinRequestResponse>>(`/api/trips/${tripId}/join-requests/${requestId}/approve`),
  rejectJoinRequest: (tripId: number, requestId: number) =>
    client.post<ApiResponse<TripJoinRequestResponse>>(`/api/trips/${tripId}/join-requests/${requestId}/reject`),
  removeMember: (tripId: number, userId: number) =>
    client.delete(`/api/trips/${tripId}/members/${userId}`),

  // Schedules
  getSchedules: (tripId: number) =>
    client.get<ApiResponse<TripScheduleResponse[]>>(`/api/trips/${tripId}/schedules`),
  createSchedule: (tripId: number, data: { date: string; title: string; memo?: string; startTime?: string; endTime?: string; orderIndex?: number }) =>
    client.post<ApiResponse<TripScheduleResponse>>(`/api/trips/${tripId}/schedules`, data),
  deleteSchedule: (tripId: number, scheduleId: number) =>
    client.delete(`/api/trips/${tripId}/schedules/${scheduleId}`),

  // Places
  getPlaces: (tripId: number) =>
    client.get<ApiResponse<TripPlaceResponse[]>>(`/api/trips/${tripId}/places`),
  createPlace: (tripId: number, data: { name: string; address?: string; category?: string; notes?: string }) =>
    client.post<ApiResponse<TripPlaceResponse>>(`/api/trips/${tripId}/places`, data),
  deletePlace: (tripId: number, placeId: number) =>
    client.delete(`/api/trips/${tripId}/places/${placeId}`),

  // Expenses
  getExpenses: (tripId: number) =>
    client.get<ApiResponse<TripExpenseResponse[]>>(`/api/trips/${tripId}/expenses`),
  getExpenseSummary: (tripId: number) =>
    client.get<ApiResponse<ExpenseSummaryResponse>>(`/api/trips/${tripId}/expenses/summary`),
  createExpense: (tripId: number, data: { category: string; amount: number; description?: string; date: string }) =>
    client.post<ApiResponse<TripExpenseResponse>>(`/api/trips/${tripId}/expenses`, data),
  deleteExpense: (tripId: number, expenseId: number) =>
    client.delete(`/api/trips/${tripId}/expenses/${expenseId}`),
}
