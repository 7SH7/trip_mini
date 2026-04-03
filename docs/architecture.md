# Trip Platform — Architecture Overview

## 1. 서비스 전체 구조도

```
                              [Client (React)]
                                    |
                                    v
                        +-----------------------+
                        |    API Gateway :8080   |
                        |  JWT 검증 + Redis 블랙 |
                        |  XSS 필터 + 보안 헤더  |
                        +-----------+-----------+
                                    |
                    +------+--------+--------+------+
                    |      |        |        |      |
              /api/users /api/trips /api/bookings  ...
                    |      |        |        |
        +-----------+------+--------+--------+-----------+
        |           |           |            |           |
   +----v----+ +----v----+ +----v-----+ +----v-----+    |
   |  User   | |  Trip   | | Booking  | | Payment  |   ...
   | :8081   | | :8082   | | :8083    | | :8084    |
   |  gRPC   | |  gRPC   | | gRPC 클라 | | Kafka   |
   |  서버   | |  서버   | | 이언트   | | Consumer |
   +---------+ +---------+ +----------+ +----------+
        |           |           |            |
        +-----+-----+-----+----+-----+------+
              |           |          |
        +-----v-----+ +---v---+ +---v---+
        |  MySQL     | | Redis | | Kafka |
        |  :3000     | | :6379 | | :9092 |
        +------------+ +-------+ +-------+


   +---------------+ +----------------+ +-------------+
   | Accommodation | | Subscription   | | Feed        |
   | :8085         | | :8086          | | :8087       |
   | 관광공사 API  | | 포트원 PG 결제 | | S3 이미지   |
   +---------------+ +----------------+ +------+------+
                                               |
   +-------------+ +----------------+    +-----v-----+
   | Chat        | | Notification   |    | MinIO S3  |
   | :8089       | | :8090          |    | :9000     |
   | WebSocket   | | SSE            |    +-----------+
   +-------------+ +----------------+

   +-------------+
   | Media       |    +---------------+
   | :8088       +--->| Nginx-RTMP    |
   | FFmpeg      |    | :1935         |
   +-------------+    +---------------+
```

## 2. 요청 흐름도

### 일반 API 요청 (인증 필요)
```
Client
  → [Authorization: Bearer JWT]
  → API Gateway (:8080)
     → JWT 서명 검증 (HMAC-SHA256)
     → Redis 블랙리스트 확인
     → XSS 필터 (입력값 sanitize)
     → X-User-Id, X-User-Email, X-User-Role 헤더 추가
     → Eureka에서 서비스 위치 조회
     → 해당 서비스로 라우팅
  → Service
     → Controller (@RequestHeader("X-User-Id") userId)
     → Service → Repository → DB
  ← ApiResponse<T> { status, message, data }
```

### 로그인 흐름
```
Client
  → POST /api/auth/login { email, password }
  → Gateway (화이트리스트, JWT 검증 없이 통과)
  → user-service
     → PasswordEncoder.matches()
     → JwtTokenProvider.createAccessToken()
     → Redis에 refreshToken 저장 (TTL: 7일)
  ← { accessToken, refreshToken }
```

### OAuth2 소셜 로그인 (Authorization Code 방식)
```
1. Client → 구글/카카오 로그인 페이지 redirect
2. 유저 로그인 → 구글/카카오가 redirect_uri로 code 전달
3. Client → POST /api/auth/google { code, redirectUri }
4. user-service:
   → code로 구글 Token API 호출 → access_token 교환 (서버 간)
   → access_token으로 구글 UserInfo API 호출 (서버 간)
   → 유저 생성/업데이트
   → JWT 발급
5. ← { accessToken, refreshToken }

* 구글 access_token은 서버에서만 사용, 프론트에 노출 안 됨
```

### 예약 + 결제 흐름 (동기 + 비동기)
```
1. Client → POST /api/bookings { tripId }
   → booking-service
      → gRPC로 user-service 유저 존재 확인 (동기)
      → gRPC로 trip-service 여행 존재 확인 (동기)
      → Booking 저장 (PENDING)
      → Outbox 테이블에 BookingCreatedEvent 저장 (같은 트랜잭션)
   ← BookingResponse

2. OutboxScheduler (1초 폴링)
   → outbox 테이블에서 PENDING 이벤트 읽기
   → Kafka "booking-events" 토픽에 발행
   → 이벤트 PUBLISHED 마킹

3. Client → POST /api/payments { bookingId, amount }
   → payment-service
      → Payment 저장 → Outbox에 PaymentCompletedEvent

4. OutboxScheduler → Kafka "payment-events" 발행

5. booking-service Kafka Consumer
   → PaymentCompletedEvent 수신
   → Booking 상태 CONFIRMED로 변경

6. notification-service Kafka Consumer
   → PaymentCompletedEvent 수신
   → 알림 생성 + SSE 푸시
```

### 채팅 흐름
```
1. Client → POST /api/chat/rooms/nearby { latitude, longitude }
   → chat-service → Haversine 쿼리로 5km 이내 방 찾기
   → 없으면 새 방 생성
   ← ChatRoomResponse

2. Client → WebSocket 연결: /ws/chat (STOMP)
   → SUBSCRIBE /topic/chat/{roomId}
   → SEND /app/chat/{roomId} { content }
   → chat-service
      → DB에 메시지 저장
      → STOMP broadcast → 같은 방 구독자에게 전달
      → Kafka "chat-events" 발행

3. notification-service
   → chat-events 수신
   → 오프라인 유저에게 알림
```

## 3. ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────────────────────────────────────┐
│                        user-service (trip_user)                 │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │       users          │                                       │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ email       VARCHAR  │ UNIQUE                                │
│  │ name        VARCHAR  │                                       │
│  │ password    VARCHAR  │ nullable (OAuth 유저)                  │
│  │ role        ENUM     │ USER, ADMIN                           │
│  │ provider    ENUM     │ LOCAL, GOOGLE, KAKAO                  │
│  │ provider_id VARCHAR  │                                       │
│  │ created_at  DATETIME │                                       │
│  │ updated_at  DATETIME │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       trip-service (trip_trip)                   │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │       trips          │                                       │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ user_id     BIGINT   │ → users.id (논리적 참조)              │
│  │ title       VARCHAR  │                                       │
│  │ description TEXT     │                                       │
│  │ start_date  DATE     │                                       │
│  │ end_date    DATE     │                                       │
│  │ status      ENUM     │ PLANNED, IN_PROGRESS, COMPLETED,      │
│  │                      │ CANCELLED                             │
│  │ created_at  DATETIME │                                       │
│  │ updated_at  DATETIME │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   booking-service (trip_booking)                 │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │      bookings        │                                       │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ user_id     BIGINT   │ → users.id                            │
│  │ trip_id     BIGINT   │ → trips.id                            │
│  │ status      ENUM     │ PENDING, CONFIRMED, CANCELLED         │
│  │ booked_at   DATETIME │                                       │
│  │ created_at  DATETIME │                                       │
│  │ updated_at  DATETIME │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   payment-service (trip_payment)                 │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │      payments        │                                       │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ booking_id  BIGINT   │ → bookings.id                         │
│  │ user_id     BIGINT   │ → users.id                            │
│  │ amount      DECIMAL  │ (10,2)                                │
│  │ status      ENUM     │ PENDING, COMPLETED, FAILED, REFUNDED  │
│  │ paid_at     DATETIME │                                       │
│  │ created_at  DATETIME │                                       │
│  │ updated_at  DATETIME │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              accommodation-service (trip_accommodation)          │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │   accommodations     │                                       │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ content_id  VARCHAR  │ UNIQUE (관광공사 ID)                   │
│  │ title       VARCHAR  │                                       │
│  │ address     VARCHAR  │                                       │
│  │ area_code   VARCHAR  │                                       │
│  │ latitude    DOUBLE   │                                       │
│  │ longitude   DOUBLE   │                                       │
│  │ image_url   VARCHAR  │                                       │
│  │ tel         VARCHAR  │                                       │
│  │ price       INT      │ 파싱된 가격                           │
│  │ price_raw   VARCHAR  │ 원본 텍스트                           │
│  │ category    VARCHAR  │                                       │
│  │ overview    TEXT     │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              subscription-service (trip_subscription)            │
│                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │    subscriptions     │  │  credit_purchases    │             │
│  ├──────────────────────┤  ├──────────────────────┤             │
│  │ id          BIGINT PK│  │ id          BIGINT PK│             │
│  │ user_id     BIGINT   │  │ user_id     BIGINT   │             │
│  │ video_call_ INT      │  │ credits     INT      │             │
│  │   credits            │  │ amount      DECIMAL  │             │
│  │ total_video INT      │  │ portone_    VARCHAR  │             │
│  │   _calls_used        │  │   payment_id        │             │
│  │ status      ENUM     │  │ status      ENUM     │             │
│  │ created_at  DATETIME │  │ created_at  DATETIME │             │
│  │ updated_at  DATETIME │  │ completed_at DATETIME│             │
│  └──────────────────────┘  └──────────────────────┘             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     feed-service (trip_feed)                     │
│                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │       feeds          │  │    feed_images       │             │
│  ├──────────────────────┤  ├──────────────────────┤             │
│  │ id          BIGINT PK│  │ id          BIGINT PK│             │
│  │ user_id     BIGINT   │  │ feed_id     BIGINT FK│──┐          │
│  │ content     TEXT     │  │ image_url   VARCHAR  │  │          │
│  │ created_at  DATETIME │  │ original_   VARCHAR  │  │          │
│  │ updated_at  DATETIME │  │   file_name         │  │          │
│  │                      │  │ created_at  DATETIME │  │          │
│  │              ◄───────┼──┼──────────────────────┘  │          │
│  └──────────────────────┘         1:N                │          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    media-service (trip_media)                    │
│                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │  transcoding_jobs    │  │    live_streams      │             │
│  ├──────────────────────┤  ├──────────────────────┤             │
│  │ id          BIGINT PK│  │ id          BIGINT PK│             │
│  │ feed_id     BIGINT   │  │ user_id     BIGINT   │             │
│  │ media_id    BIGINT   │  │ stream_key  VARCHAR  │ UNIQUE      │
│  │ input_s3_key VARCHAR │  │ title       VARCHAR  │             │
│  │ status      ENUM     │  │ status      ENUM     │ IDLE, LIVE, │
│  │ output_prefix VARCHAR│  │                      │ ENDED       │
│  │ error_message TEXT   │  │ rtmp_ingest VARCHAR  │             │
│  │ started_at  DATETIME │  │ hls_playback VARCHAR │             │
│  │ completed_at DATETIME│  │ started_at  DATETIME │             │
│  │ created_at  DATETIME │  │ ended_at    DATETIME │             │
│  └──────────────────────┘  │ created_at  DATETIME │             │
│                            └──────────────────────┘             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                     chat-service (trip_chat)                     │
│                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │     chat_rooms       │  │    chat_messages     │             │
│  ├──────────────────────┤  ├──────────────────────┤             │
│  │ id          BIGINT PK│  │ id          BIGINT PK│             │
│  │ name        VARCHAR  │  │ chat_room_id BIGINT  │             │
│  │ center_lat  DOUBLE   │  │ user_id     BIGINT   │             │
│  │ center_lng  DOUBLE   │  │ content     TEXT     │             │
│  │ radius_km   DOUBLE   │  │ type        ENUM     │ TEXT, IMAGE │
│  │ created_at  DATETIME │  │ sent_at     DATETIME │ SYSTEM      │
│  └──────────────────────┘  └──────────────────────┘             │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              notification-service (trip_notification)            │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │    notifications     │                                       │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ user_id     BIGINT   │                                       │
│  │ title       VARCHAR  │                                       │
│  │ content     TEXT     │                                       │
│  │ type        ENUM     │ BOOKING_CONFIRMED, PAYMENT_COMPLETED  │
│  │                      │ BOOKING_CANCELLED, PAYMENT_REFUNDED   │
│  │                      │ CHAT_MESSAGE, SYSTEM                  │
│  │ is_read     BOOLEAN  │                                       │
│  │ reference_id VARCHAR │                                       │
│  │ created_at  DATETIME │                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│               공통 (모든 서비스의 각 DB에 존재)                  │
│                                                                 │
│  ┌──────────────────────┐                                       │
│  │    outbox_events     │  Transactional Outbox Pattern          │
│  ├──────────────────────┤                                       │
│  │ id          BIGINT PK│                                       │
│  │ topic       VARCHAR  │  Kafka 토픽명                         │
│  │ payload     TEXT     │  JSON 직렬화된 이벤트                 │
│  │ event_type  VARCHAR  │  이벤트 클래스명                      │
│  │ status      ENUM     │  PENDING, PUBLISHED, FAILED           │
│  │ created_at  DATETIME │                                       │
│  │ published_at DATETIME│                                       │
│  └──────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘
```

**참고**: MSA에서 서비스 간 FK는 물리적으로 존재하지 않음. 각 서비스가 자체 DB를 갖고, 논리적으로만 참조함.

## 4. 배포 구조도

```
┌─────────────────────────────────────────────────────────────────┐
│                      Docker Compose (로컬 개발)                 │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │  MySQL   │  │  Redis   │  │  Kafka   │  │Zookeeper │        │
│  │  :3000   │  │  :6379   │  │  :9092   │  │  :2181   │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│                                                                 │
│  ┌──────────┐  ┌──────────┐                                     │
│  │  MinIO   │  │  Nginx   │                                     │
│  │  :9000   │  │  -RTMP   │                                     │
│  │  (S3)    │  │  :1935   │                                     │
│  └──────────┘  └──────────┘                                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    Application (로컬 실행)                       │
│                                                                 │
│  실행 순서:                                                     │
│                                                                 │
│  1. docker-compose up -d          (인프라 실행)                 │
│  2. EurekaServerApplication       (:8761)                       │
│  3. 각 서비스 Application          (:8081~8090)                 │
│  4. ApiGatewayApplication         (:8080)                       │
│  5. cd frontend && npm run dev    (:5173)                       │
│                                                                 │
│  ┌─────────────────────────────────────────────────┐            │
│  │              Eureka Server :8761                 │            │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │            │
│  │  │user    │ │trip    │ │booking │ │payment │   │            │
│  │  │:8081   │ │:8082   │ │:8083   │ │:8084   │   │            │
│  │  └────────┘ └────────┘ └────────┘ └────────┘   │            │
│  │  ┌────────┐ ┌────────┐ ┌────────┐               │            │
│  │  │accomm  │ │subscr  │ │feed    │               │            │
│  │  │:8085   │ │:8086   │ │:8087   │               │            │
│  │  └────────┘ └────────┘ └────────┘               │            │
│  │  ┌────────┐ ┌────────┐ ┌────────┐               │            │
│  │  │media   │ │chat    │ │notif   │               │            │
│  │  │:8088   │ │:8089   │ │:8090   │               │            │
│  │  └────────┘ └────────┘ └────────┘               │            │
│  └─────────────────────────────────────────────────┘            │
│                         │                                       │
│              ┌──────────v──────────┐                             │
│              │   API Gateway :8080 │                             │
│              └──────────┬──────────┘                             │
│                         │                                       │
│              ┌──────────v──────────┐                             │
│              │   Frontend :5173    │                             │
│              │   (React + Vite)    │                             │
│              └─────────────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
```

## 5. Kafka 이벤트 흐름도

```
┌──────────────┐     booking-events      ┌──────────────┐
│   Booking    │ ──────────────────────> │   Payment    │
│   Service    │                         │   Service    │
│              │ <────────────────────── │              │
└──────────────┘     payment-events      └──────────────┘
       │                                        │
       │ booking-events                         │ payment-events
       │                                        │
       v                                        v
┌──────────────────────────────────────────────────────┐
│                 Notification Service                  │
│          (booking-events, payment-events 구독)        │
│                                                      │
│  이벤트 수신 → 알림 생성 → SSE로 클라이언트 푸시     │
└──────────────────────────────────────────────────────┘

┌──────────────┐     chat-events         ┌──────────────┐
│    Chat      │ ──────────────────────> │ Notification │
│   Service    │                         │   Service    │
└──────────────┘                         └──────────────┘
```

## 6. 보안 레이어

```
Request Flow:

  Client
    │
    ▼
  [Gateway: JWT 검증]
    │  - 서명 검증 (HMAC-SHA256)
    │  - Redis 블랙리스트 확인
    │  - 화이트리스트 (/api/auth/* 등)
    │
    ▼
  [SecurityHeaderFilter: order=-1]
    │  - X-Content-Type-Options: nosniff
    │  - X-XSS-Protection: 1; mode=block
    │  - X-Frame-Options: DENY
    │  - Content-Security-Policy: default-src 'self'
    │
    ▼
  [XssFilter: order=0]
    │  - HTML 특수문자 이스케이프
    │  - <script> 태그 제거
    │  - 요청 body/params/headers sanitize
    │
    ▼
  [InternalAuthFilter: order=1]
    │  - 서비스 간 AES-GCM 토큰 검증
    │  - 30초 만료
    │
    ▼
  [Spring Security]
    │  - BCrypt 비밀번호 해싱
    │  - CSRF 비활성화 (JWT 사용)
    │  - Stateless 세션
    │
    ▼
  Controller → Service → Repository → DB
```

## 7. 기술 스택 요약

| 영역 | 기술 |
|------|------|
| Language | Kotlin 2.2.21 |
| Framework | Spring Boot 4.0.5 |
| Build | Gradle 9.4.1 (Kotlin DSL) |
| ORM | JPA + QueryDSL + MyBatis |
| DB | MySQL 8.0 |
| Cache | Redis 7 |
| Messaging | Kafka (+ Outbox Pattern) |
| Service Discovery | Eureka |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| 동기 통신 | gRPC (protobuf) |
| 비동기 통신 | Kafka |
| 인증 | JWT + OAuth2 (Authorization Code) |
| 파일 저장 | MinIO (S3 호환) |
| 스트리밍 | Nginx-RTMP + FFmpeg |
| 실시간 채팅 | WebSocket (STOMP) |
| 알림 | SSE (Server-Sent Events) |
| Frontend | React + TypeScript + Vite |
| 상태 관리 | Redux Toolkit + React Query |
| UI | MUI + Styled Components |
| Container | Docker Compose |
