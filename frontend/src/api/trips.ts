import client from './client'
import type { ApiResponse, TripResponse, CreateTripRequest } from '../types'

export const tripApi = {
  create: (data: CreateTripRequest) =>
    client.post<ApiResponse<TripResponse>>('/api/trips', data),

  getById: (id: number) =>
    client.get<ApiResponse<TripResponse>>(`/api/trips/${id}`),

  getMyTrips: () =>
    client.get<ApiResponse<TripResponse[]>>('/api/trips/my'),
}
