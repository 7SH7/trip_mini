import client from './client'
import type { ApiResponse, FeedResponse, Page } from '../types'

export const feedApi = {
  create: (content: string, images?: File[]) => {
    const formData = new FormData()
    formData.append('feed', new Blob([JSON.stringify({ content })], { type: 'application/json' }))
    images?.forEach(img => formData.append('images', img))
    return client.post<ApiResponse<FeedResponse>>('/api/feeds', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  getAll: (page = 0, size = 20) =>
    client.get<ApiResponse<Page<FeedResponse>>>('/api/feeds', { params: { page, size } }),

  getMy: (page = 0, size = 20) =>
    client.get<ApiResponse<Page<FeedResponse>>>('/api/feeds/my', { params: { page, size } }),

  getById: (id: number) =>
    client.get<ApiResponse<FeedResponse>>(`/api/feeds/${id}`),

  delete: (id: number) =>
    client.delete<ApiResponse<void>>(`/api/feeds/${id}`),
}
