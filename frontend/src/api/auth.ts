import client from './client'
import type { ApiResponse, TokenResponse, LoginRequest, RegisterRequest, OAuth2LoginRequest } from '../types'

export const authApi = {
  register: (data: RegisterRequest) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/register', data),

  login: (data: LoginRequest) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/login', data),

  logout: () =>
    client.post<ApiResponse<void>>('/api/auth/logout'),

  googleLogin: (data: OAuth2LoginRequest) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/google', data),

  kakaoLogin: (data: OAuth2LoginRequest) =>
    client.post<ApiResponse<TokenResponse>>('/api/auth/kakao', data),
}
