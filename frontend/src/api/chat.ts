import client from './client'
import type { ApiResponse, ChatRoomResponse, ChatMessageResponse, NearbyRoomRequest, Page } from '../types'

export const chatApi = {
  findNearbyRoom: (data: NearbyRoomRequest) =>
    client.post<ApiResponse<ChatRoomResponse>>('/api/chat/rooms/nearby', data),

  getRoom: (roomId: number) =>
    client.get<ApiResponse<ChatRoomResponse>>(`/api/chat/rooms/${roomId}`),

  getMessages: (roomId: number, page = 0, size = 50) =>
    client.get<ApiResponse<Page<ChatMessageResponse>>>(`/api/chat/rooms/${roomId}/messages`, { params: { page, size } }),

  joinRoom: (roomId: number) =>
    client.post<ApiResponse<void>>(`/api/chat/rooms/${roomId}/join`),

  leaveRoom: (roomId: number) =>
    client.post<ApiResponse<void>>(`/api/chat/rooms/${roomId}/leave`),

  getOnlineUsers: (roomId: number) =>
    client.get<ApiResponse<number[]>>(`/api/chat/rooms/${roomId}/online`),
}
