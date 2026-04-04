# Trip 플랫폼 기능 명세서

> 최종 업데이트: 2026-04-04

---

## 1. 사용자 서비스 (user-service :8081)

### 1.1 회원가입/로그인
| API | 메서드 | 설명 |
|---|---|---|
| `/api/auth/register` | POST | 이메일/비밀번호 회원가입, JWT 토큰 즉시 발급 |
| `/api/auth/login` | POST | 이메일/비밀번호 로그인, Access/Refresh 토큰 발급 |
| `/api/auth/refresh` | POST | Refresh 토큰으로 Access 토큰 재발급 |
| `/api/auth/logout` | POST | Access 토큰 블랙리스트 등록 + Refresh 토큰 삭제 |

### 1.2 OAuth2 소셜 로그인
| API | 메서드 | 설명 |
|---|---|---|
| `/api/auth/google` | POST | Google Authorization Code → 토큰 교환 → 유저 생성/로그인 |
| `/api/auth/kakao` | POST | Kakao Authorization Code → 토큰 교환 → 유저 생성/로그인 |

- 신규 유저: 자동 회원가입 후 JWT 발급
- 기존 유저: OAuth2 정보 업데이트 후 JWT 발급

### 1.3 사용자 관리
| API | 메서드 | 설명 |
|---|---|---|
| `/api/users` | POST | 사용자 생성 (내부 서비스용) |
| `/api/users/{id}` | GET | 사용자 조회 |
| `/api/users/me` | GET | 현재 로그인 사용자 정보 조회 (X-User-Id 헤더) |

### 1.4 보안
- JWT (HMAC-SHA256): Access Token 1시간, Refresh Token 7일
- Redis: Refresh 토큰 저장 + Access 토큰 블랙리스트
- AES-256-GCM: 이메일 등 개인정보 DB 암호화 (`EncryptedStringConverter`)
- Spring Security: 비밀번호 BCrypt 해싱
- gRPC 서버 (:9091): 다른 서비스에서 유저 존재 확인용

---

## 2. 여행 서비스 (trip-service :8082)

### 2.1 여행 CRUD
| API | 메서드 | 설명 |
|---|---|---|
| `/api/trips` | POST | 여행 생성 (생성자 자동 OWNER 등록) |
| `/api/trips/{id}` | GET | 여행 상세 조회 |
| `/api/trips/my` | GET | 내가 참여 중인 모든 여행 조회 (팀 여행 포함) |
| `/api/trips/search` | GET | 여행 검색 (키워드, 상태, 날짜 필터 — QueryDSL) |

- 여행 상태: `PLANNED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`
- 생성 시 Kafka 이벤트 발행 → notification-service에서 알림 생성

### 2.2 여행 팟 (팀) 관리 — 보상 트랜잭션
| API | 메서드 | 설명 |
|---|---|---|
| `/api/trips/{tripId}/members` | GET | 팟 멤버 목록 조회 |
| `/api/trips/{tripId}/members/invite` | POST | 8자리 초대 코드 생성 (7일 유효) |
| `/api/trips/join` | POST | 초대 코드로 참여 요청 (PENDING 상태) |
| `/api/trips/{tripId}/join-requests` | GET | 대기 중 참여 요청 목록 (방장용) |
| `/api/trips/{tripId}/join-requests/{id}/approve` | POST | 참여 승인 → 멤버 추가 + 알림 |
| `/api/trips/{tripId}/join-requests/{id}/reject` | POST | 참여 거절 → 요청 취소 + 알림 |
| `/api/trips/{tripId}/members/{userId}` | DELETE | 멤버 내보내기 (방장) / 탈퇴 (본인) |

**Saga 플로우:**
```
코드 입력 → PENDING 요청 생성 → 방장 알림
  ├─ 승인 (Confirm) → 멤버 추가 → 요청자 알림
  └─ 거절 (Compensate) → 요청 취소 → 요청자 알림
```

**제약:**
- 여행당 최대 5명
- 방장(OWNER)만 승인/거절/내보내기 가능
- 방장은 내보낼 수 없음
- 중복 요청 방지

### 2.3 여행 일정 (캘린더)
| API | 메서드 | 설명 |
|---|---|---|
| `/api/trips/{tripId}/schedules` | GET | 일정 목록 (날짜순 정렬) |
| `/api/trips/{tripId}/schedules` | POST | 일정 추가 (날짜, 제목, 메모, 시작/종료 시간) |
| `/api/trips/{tripId}/schedules/{id}` | PUT | 일정 수정 |
| `/api/trips/{tripId}/schedules/{id}` | DELETE | 일정 삭제 |

- 여행 기간 내 날짜별 일정 관리
- 팟 멤버만 CRUD 가능

### 2.4 여행 장소
| API | 메서드 | 설명 |
|---|---|---|
| `/api/trips/{tripId}/places` | GET | 장소 목록 조회 |
| `/api/trips/{tripId}/places` | POST | 장소 추가 (이름, 주소, 좌표, 카테고리, 메모) |
| `/api/trips/{tripId}/places/{id}` | PUT | 장소 수정 |
| `/api/trips/{tripId}/places/{id}` | DELETE | 장소 삭제 |

- 카테고리: 관광, 맛집, 카페, 숙소, 쇼핑, 기타
- 팟 멤버만 CRUD 가능

### 2.5 여행 가계부
| API | 메서드 | 설명 |
|---|---|---|
| `/api/trips/{tripId}/expenses` | GET | 지출 내역 (날짜순) |
| `/api/trips/{tripId}/expenses` | POST | 지출 추가 (카테고리, 금액, 설명, 날짜) |
| `/api/trips/{tripId}/expenses/{id}` | PUT | 지출 수정 |
| `/api/trips/{tripId}/expenses/{id}` | DELETE | 지출 삭제 |
| `/api/trips/{tripId}/expenses/summary` | GET | 지출 요약 (총액, 카테고리별, 멤버별) |

- 카테고리: 식비, 교통, 숙박, 관광, 쇼핑, 기타
- 누가 얼마 썼는지 멤버별 집계

### 2.6 gRPC 서버 (:9082)
- `GetTrip`: 여행 상세 조회 (booking-service에서 호출)
- `CheckTripExists`: 여행 존재 확인

---

## 3. 예약 서비스 (booking-service :8083)

| API | 메서드 | 설명 |
|---|---|---|
| `/api/bookings` | POST | 예약 생성 (gRPC로 유저/여행 존재 검증) |
| `/api/bookings/{id}` | GET | 예약 조회 |
| `/api/bookings/my` | GET | 내 예약 목록 |
| `/api/bookings/{id}/confirm` | PATCH | 예약 확정 |
| `/api/bookings/{id}/cancel` | PATCH | 예약 취소 |

- 예약 상태: `PENDING` → `CONFIRMED` / `CANCELLED`
- 30분 내 결제 미완료 시 자동 취소 (`BookingTimeoutScheduler`)
- Kafka 이벤트: 예약 생성/확정/취소 → payment-service, notification-service
- gRPC 클라이언트: user-service, trip-service 호출
- QueryDSL 동적 검색 + MyBatis 통계 쿼리

---

## 4. 결제 서비스 (payment-service :8084)

| API | 메서드 | 설명 |
|---|---|---|
| `/api/payments` | POST | 결제 생성 (bookingId, amount) |
| `/api/payments/my` | GET | 내 결제 내역 |
| `/api/payments/{id}` | GET | 결제 조회 |
| `/api/payments/{id}/complete` | PATCH | 결제 완료 → Kafka 이벤트 발행 |
| `/api/payments/{id}/refund` | PATCH | 환불 처리 → Kafka 이벤트 발행 |
| `/api/payments/{id}/fail` | PATCH | 결제 실패 → Kafka 이벤트 발행 |

- 결제 상태: `PENDING` → `COMPLETED` / `FAILED` / `REFUNDED`
- Kafka Consumer: booking-events 수신하여 결제 자동 처리
- Outbox 패턴으로 이벤트 발행
- MyBatis: 일별 매출 집계, 상태별 결제 수 통계

---

## 5. 숙소 서비스 (accommodation-service :8085)

| API | 메서드 | 설명 |
|---|---|---|
| `/api/accommodations/search` | GET | 숙소 검색 (keyword 또는 areaCode) |
| `/api/accommodations/{id}` | GET | 숙소 상세 조회 |

- 한국관광공사 Tour API 연동 (`https://apis.data.go.kr/B551011/KorService1`)
- DB 캐싱: 검색 결과를 로컬 DB에 저장, 재검색 시 캐시 우선 반환
- 가격 파싱: "10,000원~" 형태의 텍스트를 숫자로 변환 (`PriceParser`)
- **환경변수 필요**: `TOUR_API_KEY`

---

## 6. 구독 서비스 (subscription-service :8086)

### 6.1 구독/크레딧 관리
| API | 메서드 | 설명 |
|---|---|---|
| `/api/subscriptions/my` | GET | 내 구독 정보 (크레딧 잔액, 총 사용량) |
| `/api/subscriptions/video-call/use` | POST | 영상통화 크레딧 1회 사용 |
| `/api/subscriptions/credits/purchase` | POST | 크레딧 충전 (10회 / 5,000원) |
| `/api/subscriptions/credits/history` | GET | 충전 내역 |
| `/api/subscriptions/check` | GET | 크레딧 보유 여부 확인 |

### 6.2 PortOne 결제 연동
- 프론트에서 PortOne SDK로 카드 결제 → `imp_uid` 전달 → 서버에서 PortOne API로 결제 검증
- 결제 금액/상태 불일치 시 실패 처리
- **환경변수 필요**: `PORTONE_API_SECRET`, `PORTONE_STORE_ID`

### 6.3 gRPC 서버 (:9086)
- `CheckCredits`: 크레딧 잔액 확인 (chat-service에서 영상통화 전 호출)
- `UseVideoCall`: 크레딧 차감

---

## 7. 피드 서비스 (feed-service :8087)

| API | 메서드 | 설명 |
|---|---|---|
| `/api/feeds` | POST | 피드 작성 (텍스트 + 이미지 다중 업로드) |
| `/api/feeds` | GET | 전체 피드 조회 (페이징) |
| `/api/feeds/my` | GET | 내 피드 조회 (페이징) |
| `/api/feeds/{id}` | GET | 피드 상세 |
| `/api/feeds/{id}` | PUT | 피드 수정 (본인만) |
| `/api/feeds/{id}` | DELETE | 피드 삭제 (본인만) |

- 이미지 저장: MinIO (S3 호환) → `feeds/{uuid}.{ext}` 경로
- 버킷 자동 생성 (`@PostConstruct`)
- `Feed` ↔ `FeedImage` 1:N 관계

---

## 8. 미디어 서비스 (media-service :8088)

### 8.1 라이브 스트리밍
| API | 메서드 | 설명 |
|---|---|---|
| `/api/media/live/streams` | POST | 라이브 스트림 생성 (streamKey 자동 발급) |
| `/api/media/live/streams` | GET | 현재 방송 중인 스트림 목록 |
| `/api/media/live/streams/{id}` | GET | 스트림 상세 |
| `/api/media/live/streams/my` | GET | 내 스트림 목록 |
| `/api/media/live/on-publish` | POST | nginx-RTMP 콜백: 방송 시작 |
| `/api/media/live/on-done` | POST | nginx-RTMP 콜백: 방송 종료 |

- RTMP 수신 → nginx-rtmp → HLS 변환 (3초 세그먼트)
- 스트림 상태: `IDLE` → `LIVE` → `ENDED`
- OBS 등에서 `rtmp://localhost:1935/live/{streamKey}`로 송출

### 8.2 VOD 트랜스코딩
| API | 메서드 | 설명 |
|---|---|---|
| `/api/media/transcoding/{id}` | GET | 트랜스코딩 작업 상태 조회 |

- Kafka Consumer: `media-events` 토픽 수신 → 트랜스코딩 자동 시작
- 파이프라인: S3 다운로드 → FFmpeg (720p/480p/360p HLS) → S3 업로드
- Master Playlist (ABR: Adaptive Bitrate Streaming)
- 작업 상태: `QUEUED` → `PROCESSING` → `COMPLETED` / `FAILED`

---

## 9. 채팅 서비스 (chat-service :8089)

### 9.1 REST API
| API | 메서드 | 설명 |
|---|---|---|
| `/api/chat/rooms/nearby` | POST | GPS 좌표 기반 반경 5km 채팅방 찾기/생성 |
| `/api/chat/rooms/{roomId}` | GET | 채팅방 정보 |
| `/api/chat/rooms/{roomId}/messages` | GET | 메시지 히스토리 (페이징) |
| `/api/chat/rooms/{roomId}/join` | POST | 채팅방 입장 (Redis 온라인 추적) |
| `/api/chat/rooms/{roomId}/leave` | POST | 채팅방 퇴장 |
| `/api/chat/rooms/{roomId}/online` | GET | 접속 중 유저 목록 |

### 9.2 WebSocket (STOMP)
| 경로 | 설명 |
|---|---|
| `/ws/chat` (SockJS) | WebSocket 연결 엔드포인트 |
| `/app/chat/{roomId}` | 메시지 전송 (Publish) |
| `/topic/chat/{roomId}` | 실시간 메시지 수신 (Subscribe) |

- Haversine 공식으로 GPS 거리 계산 (JPQL)
- Redis: 온라인 유저 Set 관리
- 메시지 타입: `TEXT`, `IMAGE`, `SYSTEM`
- Kafka: 채팅 메시지 이벤트 → notification-service

---

## 10. 알림 서비스 (notification-service :8090)

### 10.1 REST API
| API | 메서드 | 설명 |
|---|---|---|
| `/api/notifications/subscribe` | GET | SSE 연결 (실시간 알림 푸시) |
| `/api/notifications` | GET | 알림 목록 (페이징) |
| `/api/notifications/unread-count` | GET | 읽지 않은 알림 수 |
| `/api/notifications/{id}/read` | PATCH | 알림 읽음 처리 |
| `/api/notifications/read-all` | PATCH | 전체 읽음 처리 |

### 10.2 Kafka 이벤트 소비
| 토픽 | 이벤트 | 알림 내용 |
|---|---|---|
| `booking-events` | BookingConfirmedEvent | "예약 #N이 확정되었습니다" |
| `booking-events` | BookingCancelledEvent | "예약 #N이 취소되었습니다" |
| `payment-events` | PaymentCompletedEvent | "결제 #N이 완료되었습니다 (N원)" |
| `payment-events` | PaymentFailedEvent | "결제 실패, 예약 자동 취소" |
| `payment-events` | PaymentRefundedEvent | "결제 #N이 환불되었습니다" |
| `user-events` | UserCreatedEvent | "Trip 서비스에 가입 환영" |
| `trip-events` | TripCreatedEvent | "여행 생성 완료" |
| `trip-events` | TripJoinRequestedEvent | "User #N이 참여 요청" (방장에게) |
| `trip-events` | TripJoinApprovedEvent | "참여 승인됨" (요청자에게) |
| `trip-events` | TripJoinRejectedEvent | "참여 거절됨" (요청자에게) |
| `chat-events` | ChatMessageEvent | 로깅 (GPS 채팅방은 고정 멤버 없음) |

- SSE (Server-Sent Events): 실시간 푸시
- 알림 타입: `BOOKING_CONFIRMED`, `BOOKING_CANCELLED`, `PAYMENT_COMPLETED`, `PAYMENT_FAILED`, `PAYMENT_REFUNDED`, `CHAT_MESSAGE`, `SYSTEM`

---

## 11. API Gateway (api-gateway :8080)

| 기능 | 설명 |
|---|---|
| JWT 인증 | Bearer 토큰 검증 → X-User-Id, X-User-Email, X-User-Role 헤더 주입 |
| 토큰 블랙리스트 | Redis에서 로그아웃된 토큰 확인 |
| 라우팅 | 11개 서비스로 경로 기반 라우팅 (Eureka 로드밸런싱) |
| WebSocket | `/ws/chat/**` → chat-service (WebSocket 프록시) |
| 인증 제외 경로 | `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/google`, `/api/auth/kakao` |

---

## 12. 서비스 디스커버리 (eureka-server :8761)

- Netflix Eureka 서비스 레지스트리
- 모든 마이크로서비스 자동 등록/탐색
- Self-preservation 비활성화 (개발 환경)

---

## 13. 공통 모듈 (common)

### 13.1 보안
| 컴포넌트 | 설명 |
|---|---|
| `XssFilter` + `XssRequestWrapper` + `XssSanitizer` | XSS 공격 방지 (파라미터/헤더/바디 필터링) |
| `SecurityHeaderFilter` | X-Content-Type-Options, X-Frame-Options, CSP 헤더 |
| `InternalAuthFilter` + `InternalTokenProvider` | 서비스 간 내부 인증 (AES-GCM 토큰, 30초 유효) |
| `Aes256Encryptor` + `EncryptedStringConverter` | JPA 필드 레벨 AES-256-GCM 자동 암복호화 |

### 13.2 이벤트 시스템
| 컴포넌트 | 설명 |
|---|---|
| `OutboxEventPublisher` | 트랜잭션 내 이벤트를 outbox 테이블에 저장 |
| `OutboxScheduler` | 1초 주기로 PENDING 이벤트를 Kafka로 발행 |
| `DomainEvent` | 모든 이벤트의 베이스 클래스 (Jackson 다형성 직렬화) |

### 13.3 예외 처리
- `GlobalExceptionHandler`: 전역 에러 핸들링 (400, 404, 503, 500)
- `ApiResponse<T>`: 통일된 응답 포맷 `{ status, message, data }`

---

## 14. 프론트엔드 (React + TypeScript)

### 14.1 기술 스택
| 기술 | 용도 |
|---|---|
| React 19 + TypeScript | UI 프레임워크 |
| Vite | 빌드 도구 |
| Material-UI 7 | UI 컴포넌트 |
| styled-components | 커스텀 스타일링 |
| Redux Toolkit | 전역 상태 (인증) |
| TanStack Query | 서버 상태 관리 |
| STOMP.js + SockJS | 실시간 채팅 |
| HLS.js | 라이브 스트리밍 재생 |
| PortOne (iamport) SDK | PG 결제 |

### 14.2 페이지 구성
| 페이지 | 경로 | 기능 |
|---|---|---|
| 홈 | `/` | 히어로 섹션 + 6개 기능 카드 |
| 로그인 | `/login` | 이메일 + Google/Kakao OAuth2 |
| 회원가입 | `/register` | 이메일 가입 |
| OAuth 콜백 | `/oauth/callback` | 소셜 로그인 처리 |
| 내 여행 | `/trips` | 여행 목록 + 초대 코드 참여 |
| 여행 만들기 | `/trips/new` | 여행 생성 폼 |
| 여행 상세 | `/trips/:id` | 4탭 대시보드 (멤버/일정/장소/가계부) |
| 예약 | `/bookings` | 예약 목록 + 결제/취소 |
| 결제 내역 | `/payments` | 결제 목록 + 환불 |
| 숙소 검색 | `/accommodations` | 키워드 검색 + 카드 목록 |
| 구독 관리 | `/subscription` | 크레딧 잔액 + 충전 + 영상통화 사용 |
| 피드 | `/feed` | SNS 형태 글/사진 공유 |
| GPS 채팅 | `/chat` | 위치 기반 채팅방 찾기 |
| 채팅방 | `/chat/:roomId` | 실시간 메시지 + 접속자 표시 |
| 알림 | `/notifications` | 알림 목록 + 읽음 처리 |
| 라이브 | `/streaming` | 방송 목록 + 생성 + HLS 재생 |

### 14.3 UI/UX 특징
- 사이드바 네비게이션 (데스크탑) + 햄버거 드로어 (모바일)
- Glassmorphism 상단바 (blur 효과)
- 그라디언트 히어로/카드/버튼
- Card hover 애니메이션
- 메신저 스타일 채팅 말풍선
- Inter 폰트 + 커스텀 스크롤바

---

## 15. 인프라 (docker-compose)

| 서비스 | 이미지 | 포트 | 용도 |
|---|---|---|---|
| MySQL 8.0 | `mysql:8.0` | 3000→3306 | 전 서비스 DB |
| Redis 7 | `redis:7-alpine` | 6379 | JWT 블랙리스트, 채팅 온라인 추적 |
| Kafka | `cp-kafka:7.6.0` | 9092 | 이벤트 스트리밍 |
| Zookeeper | `cp-zookeeper:7.6.0` | 2181 | Kafka 메타데이터 |
| MinIO | `minio/minio` | 9000, 9001 | S3 호환 오브젝트 스토리지 |
| nginx-rtmp | `tiangolo/nginx-rtmp` | 1935, 8888 | RTMP→HLS 라이브 스트리밍 |

---

## 16. Swagger API 문서

각 서비스 실행 후 `http://localhost:{port}/swagger-ui/index.html` 접속

| 서비스 | Swagger UI |
|---|---|
| user-service | `localhost:8081/swagger-ui/index.html` |
| trip-service | `localhost:8082/swagger-ui/index.html` |
| booking-service | `localhost:8083/swagger-ui/index.html` |
| payment-service | `localhost:8084/swagger-ui/index.html` |
| accommodation-service | `localhost:8085/swagger-ui/index.html` |
| subscription-service | `localhost:8086/swagger-ui/index.html` |
| feed-service | `localhost:8087/swagger-ui/index.html` |
| media-service | `localhost:8088/swagger-ui/index.html` |
| chat-service | `localhost:8089/swagger-ui/index.html` |
| notification-service | `localhost:8090/swagger-ui/index.html` |
