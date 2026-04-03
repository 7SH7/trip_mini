import client from './client'
import type { ApiResponse, TokenResponse, LoginRequest, RegisterRequest } from '../types'

export const authApi = {
  register: (data: RegisterRequest) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/register', data),

  login: (data: LoginRequest) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/login', data),

  logout: () =>
    client.post<ApiResponse<void>>('/api/auth/logout'),

  googleLogin: (accessToken: string) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/google', { accessToken }),

  kakaoLogin: (accessToken: string) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/kakao', { accessToken }),
}
