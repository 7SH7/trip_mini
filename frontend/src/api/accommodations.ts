import client from './client'
import type { ApiResponse, AccommodationResponse, Page } from '../types'

export const accommodationApi = {
  search: (params: { keyword?: string; areaCode?: string; page?: number; size?: number }) =>
    client.get<ApiResponse<Page<AccommodationResponse>>>('/api/accommodations/search', { params }),

  getById: (id: number) =>
    client.get<ApiResponse<AccommodationResponse>>(`/api/accommodations/${id}`),
}
