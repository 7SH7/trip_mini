export interface ApiResponse<T> {
  status: number
  message: string
  data: T | null
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
}

export interface UserResponse {
  id: number
  email: string
  name: string
  createdAt: string
}

export interface TripResponse {
  id: number
  userId: number
  title: string
  description: string | null
  startDate: string
  endDate: string
  status: 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
}

export interface BookingResponse {
  id: number
  userId: number
  tripId: number
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED'
  bookedAt: string
  createdAt: string
}

export interface PaymentResponse {
  id: number
  bookingId: number
  userId: number
  amount: number
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'
  paidAt: string | null
  createdAt: string
}

export interface RegisterRequest {
  email: string
  name: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface CreateTripRequest {
  title: string
  description?: string
  startDate: string
  endDate: string
}

export interface CreateBookingRequest {
  tripId: number
}

export interface CreatePaymentRequest {
  bookingId: number
  amount: number
}
