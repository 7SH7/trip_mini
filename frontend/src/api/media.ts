import client from './client'
import type { ApiResponse, LiveStreamResponse } from '../types'

export const mediaApi = {
  createStream: (title: string) =>
    client.post<ApiResponse<LiveStreamResponse>>('/api/media/live/streams', { title }),

  getActiveStreams: () =>
    client.get<ApiResponse<LiveStreamResponse[]>>('/api/media/live/streams'),

  getStream: (id: number) =>
    client.get<ApiResponse<LiveStreamResponse>>(`/api/media/live/streams/${id}`),

  getMyStreams: () =>
    client.get<ApiResponse<LiveStreamResponse[]>>('/api/media/live/streams/my'),
}
