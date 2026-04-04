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

export interface AccommodationResponse {
  id: number
  contentId: string
  title: string
  address: string | null
  imageUrl: string | null
  tel: string | null
  price: number | null
  priceRaw: string | null
  latitude: number | null
  longitude: number | null
  category: string | null
}

export interface SubscriptionResponse {
  id: number
  userId: number
  videoCallCredits: number
  totalVideoCallsUsed: number
  status: 'ACTIVE' | 'SUSPENDED'
}

export interface UseVideoCallResponse {
  success: boolean
  remainingCredits: number
  message: string
}

export interface CreditPurchaseResponse {
  id: number
  credits: number
  amount: number
  status: string
}

export interface FeedResponse {
  id: number
  userId: number
  content: string
  images: FeedImageResponse[]
  createdAt: string
  updatedAt: string
}

export interface FeedImageResponse {
  id: number
  imageUrl: string
  originalFileName: string | null
}

export interface ChatRoomResponse {
  id: number
  name: string
  centerLatitude: number
  centerLongitude: number
  onlineCount: number
}

export interface ChatMessageResponse {
  id: number
  chatRoomId: number
  userId: number
  content: string
  type: 'TEXT' | 'IMAGE' | 'SYSTEM'
  sentAt: string
}

export interface NotificationResponse {
  id: number
  title: string
  content: string
  type: string
  isRead: boolean
  referenceId: string | null
  createdAt: string
}

export interface LiveStreamResponse {
  id: number
  userId: number
  streamKey: string
  title: string
  status: 'IDLE' | 'LIVE' | 'ENDED'
  rtmpIngestUrl: string | null
  hlsPlaybackUrl: string | null
  startedAt: string | null
  createdAt: string
}

// Trip Team
export interface TripMemberResponse {
  id: number; tripId: number; userId: number; role: 'OWNER' | 'MEMBER'; joinedAt: string
}
export interface InviteCodeResponse {
  code: string; tripId: number; expiresAt: string
}
export interface TripScheduleResponse {
  id: number; tripId: number; date: string; title: string; memo: string | null
  startTime: string | null; endTime: string | null; orderIndex: number; createdAt: string
}
export interface TripPlaceResponse {
  id: number; tripId: number; name: string; address: string | null
  latitude: number | null; longitude: number | null; category: string | null
  notes: string | null; addedBy: number | null; createdAt: string
}
export interface TripExpenseResponse {
  id: number; tripId: number; userId: number; category: string
  amount: number; description: string | null; date: string; createdAt: string
}
export interface ExpenseSummaryResponse {
  totalExpense: number; byCategory: Record<string, number>; byUser: Record<string, number>
}

export interface TripJoinRequestResponse {
  id: number; tripId: number; userId: number; inviteCode: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'; createdAt: string; processedAt: string | null
}

// Requests
export interface RegisterRequest { email: string; name: string; password: string }
export interface LoginRequest { email: string; password: string }
export interface OAuth2LoginRequest { code: string; redirectUri: string }
export interface CreateTripRequest { title: string; description?: string; startDate: string; endDate: string }
export interface CreateBookingRequest { tripId: number }
export interface CreatePaymentRequest { bookingId: number; amount: number }
export interface PurchaseCreditsRequest { portonePaymentId: string }
export interface NearbyRoomRequest { latitude: number; longitude: number }

// Paged
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
