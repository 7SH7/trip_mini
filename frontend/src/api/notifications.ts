import client from './client'
import type { ApiResponse, NotificationResponse, Page } from '../types'

export const notificationApi = {
  getAll: (page = 0, size = 20) =>
    client.get<ApiResponse<Page<NotificationResponse>>>('/api/notifications', { params: { page, size } }),

  getUnreadCount: () =>
    client.get<ApiResponse<{ count: number }>>('/api/notifications/unread-count'),

  markAsRead: (id: number) =>
    client.patch<ApiResponse<void>>(`/api/notifications/${id}/read`),

  markAllAsRead: () =>
    client.patch<ApiResponse<void>>('/api/notifications/read-all'),
}
