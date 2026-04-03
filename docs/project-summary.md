# Trip Platform — 프로젝트 종합 정리

## 프로젝트 소개

여행 플랫폼 MSA 프로젝트. 숙소 조회, 여행 예약/결제, 실시간 채팅, 피드, 라이브 스트리밍, 구독제 결제를 마이크로서비스 아키텍처로 구현했다.

---

## 1. 기술 스택

| 영역 | 기술 |
|------|------|
| **Language** | Kotlin 2.2.21 |
| **Framework** | Spring Boot 4.0.5 + Spring Cloud 2025.0.0 |
| **Build** | Gradle 9.4.1 (Kotlin DSL), 멀티모듈 모노레포 |
| **ORM/DB** | JPA (ddl-auto: update) + QueryDSL (동적 쿼리) + MyBatis (통계 SQL) |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 |
| **Messaging** | Kafka + Outbox Pattern (트랜잭션 일관성) |
| **Service Discovery** | Eureka |
| **API Gateway** | Spring Cloud Gateway (WebFlux) |
| **동기 통신** | gRPC (protobuf) |
| **비동기 통신** | Kafka |
| **인증** | JWT + OAuth2 (Authorization Code 방식, Google/Kakao) |
| **암호화** | AES-256-GCM (민감정보), BCrypt (비밀번호), AES-GCM (내부 인증) |
| **보안** | XSS 필터, 보안 헤더 (CSP, X-Frame-Options 등) |
| **파일 저장** | MinIO (S3 호환) |
| **스트리밍** | Nginx-RTMP + FFmpeg (HLS 트랜스코딩) |
| **실시간 채팅** | WebSocket (STOMP over SockJS) |
| **알림** | SSE (Server-Sent Events) |
| **PG 결제** | 포트원 (iamport) 테스트 모드 |
| **Frontend** | React 19 + TypeScript + Vite |
| **상태 관리** | Redux Toolkit (전역) + React Query (서버) |
| **UI** | MUI (Material UI) + Styled Components |
| **Container** | Docker + Docker Compose |

---

## 2. 서비스 구성 (13개 모듈)

```
trip/
├── eureka-server          :8761  Service Discovery
├── api-gateway            :8080  JWT 검증 + 라우팅 + Redis 블랙리스트
├── common                        공통 라이브러리
├── proto                         gRPC protobuf 정의
│
├── user-service           :8081  인증/유저/OAuth2       (gRPC 서버 :9091)
├── trip-service           :8082  여행 CRUD              (gRPC 서버 :9082)
├── booking-service        :8083  예약 + gRPC 클라이언트 + Kafka Consumer
├── payment-service        :8084  결제 + Kafka Consumer
│
├── accommodation-service  :8085  숙소 조회 (한국관광공사 Tour API)
├── subscription-service   :8086  구독/크레딧 (포트원 PG) (gRPC 서버 :9086)
├── feed-service           :8087  피드 (사진+글, S3 업로드)
├── media-service          :8088  트랜스코딩 (FFmpeg) + 스트리밍 (RTMP)
├── chat-service           :8089  GPS 채팅 (WebSocket STOMP)
├── notification-service   :8090  알림 (SSE, Kafka 다중 토픽)
│
├── frontend/                     React + TypeScript (Vite)
├── docker-compose.yml            인프라
└── docs/                         문서
```

---

## 3. 인프라 (Docker Compose)

| 서비스 | 포트 | 용도 |
|--------|------|------|
| MySQL 8.0 | 3000 | 전체 DB (10개 스키마, 서비스별 분리) |
| Redis 7 | 6379 | JWT 토큰, 채팅 온라인 유저, 캐시 |
| Kafka + Zookeeper | 9092/2181 | 이벤트 메시징 |
| MinIO | 9000/9001 | S3 호환 파일 스토리지 |
| Nginx-RTMP | 1935/8888 | 실시간 스트리밍 |

---

## 4. API 엔드포인트 전체 목록

### user-service (:8081)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/auth/register | 회원가입 | X |
| POST | /api/auth/login | 로그인 | X |
| POST | /api/auth/refresh | 토큰 갱신 | X |
| POST | /api/auth/logout | 로그아웃 | O |
| POST | /api/auth/google | 구글 OAuth2 (Authorization Code) | X |
| POST | /api/auth/kakao | 카카오 OAuth2 (Authorization Code) | X |
| GET | /api/users/me | 내 정보 조회 | O |
| GET | /api/users/{id} | 유저 조회 | O |
| POST | /api/users | 유저 생성 | O |

### trip-service (:8082)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/trips | 여행 생성 | O |
| GET | /api/trips/{id} | 여행 상세 | O |
| GET | /api/trips/my | 내 여행 목록 | O |
| GET | /api/trips/search | 여행 검색 (QueryDSL) | O |

### booking-service (:8083)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/bookings | 예약 생성 (gRPC로 유저/여행 검증) | O |
| GET | /api/bookings/{id} | 예약 상세 | O |
| GET | /api/bookings/my | 내 예약 목록 | O |
| PATCH | /api/bookings/{id}/confirm | 예약 확정 | O |
| PATCH | /api/bookings/{id}/cancel | 예약 취소 → Kafka → 자동 환불 | O |

### payment-service (:8084)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/payments | 결제 생성 | O |
| GET | /api/payments/{id} | 결제 상세 | O |
| PATCH | /api/payments/{id}/complete | 결제 완료 → Kafka → 예약 확정 | O |
| PATCH | /api/payments/{id}/refund | 환불 | O |

### accommodation-service (:8085)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | /api/accommodations/search | 숙소 검색 (관광공사 API + DB 캐시) | O |
| GET | /api/accommodations/{id} | 숙소 상세 | O |

### subscription-service (:8086)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | /api/subscriptions/my | 내 구독/크레딧 조회 | O |
| POST | /api/subscriptions/video-call/use | 영상통화 크레딧 사용 (-1) | O |
| POST | /api/subscriptions/credits/purchase | 크레딧 충전 (포트원 결제 검증) | O |
| GET | /api/subscriptions/credits/history | 충전 내역 | O |
| GET | /api/subscriptions/check?userId= | 크레딧 확인 (내부용) | 내부 |

### feed-service (:8087)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/feeds | 피드 생성 (멀티파트 이미지) | O |
| GET | /api/feeds | 전체 피드 (페이지네이션) | O |
| GET | /api/feeds/my | 내 피드 | O |
| GET | /api/feeds/{id} | 피드 상세 | O |
| PUT | /api/feeds/{id} | 피드 수정 (본인만) | O |
| DELETE | /api/feeds/{id} | 피드 삭제 (본인만) | O |

### media-service (:8088)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | /api/media/transcoding/{id} | 트랜스코딩 작업 상태 | O |
| POST | /api/media/live/streams | 라이브 스트림 생성 | O |
| GET | /api/media/live/streams | 활성 스트림 목록 | O |
| GET | /api/media/live/streams/{id} | 스트림 상세 | O |
| GET | /api/media/live/streams/my | 내 스트림 | O |
| POST | /api/media/live/on-publish | Nginx-RTMP 콜백 | 내부 |
| POST | /api/media/live/on-done | Nginx-RTMP 콜백 | 내부 |

### chat-service (:8089)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/chat/rooms/nearby | GPS 근처 채팅방 찾기/생성 | O |
| GET | /api/chat/rooms/{id} | 채팅방 정보 | O |
| GET | /api/chat/rooms/{id}/messages | 메시지 히스토리 | O |
| GET | /api/chat/rooms/{id}/online | 접속 중인 유저 | O |
| POST | /api/chat/rooms/{id}/join | 채팅방 입장 | O |
| POST | /api/chat/rooms/{id}/leave | 채팅방 퇴장 | O |
| WS | /ws/chat | WebSocket STOMP 엔드포인트 | — |

### notification-service (:8090)
| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| GET | /api/notifications/subscribe | SSE 알림 구독 | O |
| GET | /api/notifications | 알림 목록 (페이지네이션) | O |
| GET | /api/notifications/unread-count | 안 읽은 알림 수 | O |
| PATCH | /api/notifications/{id}/read | 읽음 처리 | O |
| PATCH | /api/notifications/read-all | 전체 읽음 처리 | O |

---

## 5. Kafka 이벤트 설계

| 토픽 | Producer | Consumer | 이벤트 |
|------|----------|----------|--------|
| booking-events | booking-service | payment-service, notification-service | BookingCreated, Confirmed, Cancelled |
| payment-events | payment-service | booking-service, notification-service | PaymentCompleted, Refunded |
| user-events | user-service | (확장 예정) | UserCreated |
| trip-events | trip-service | (확장 예정) | TripCreated |
| chat-events | chat-service | notification-service | 채팅 메시지 |
| media-events | feed-service | media-service | VideoUploaded, TranscodingCompleted |

### Outbox Pattern
모든 이벤트는 `KafkaTemplate`이 아닌 `OutboxEventPublisher`로 발행. DB와 같은 트랜잭션에 `outbox_events` 테이블에 저장 → `OutboxScheduler`가 1초마다 폴링하여 Kafka 발행.

---

## 6. gRPC 서비스 간 통신

| 호출자 | 대상 | RPC | 용도 |
|--------|------|-----|------|
| booking-service | user-service :9091 | CheckUserExists | 예약 시 유저 존재 확인 |
| booking-service | trip-service :9082 | CheckTripExists | 예약 시 여행 존재 확인 |
| (chat-service) | subscription-service :9086 | CheckCredits, UseVideoCall | 영상통화 크레딧 확인/사용 |

---

## 7. 보안 레이어

```
요청 흐름:

Client → Gateway (JWT 검증 + Redis 블랙리스트)
       → SecurityHeaderFilter (X-Content-Type-Options, X-XSS-Protection, X-Frame-Options, CSP)
       → XssFilter (HTML 이스케이프, <script> 제거)
       → InternalAuthFilter (서비스 간 AES-GCM 토큰 검증)
       → Spring Security (BCrypt, Stateless)
       → Controller
```

| 보안 항목 | 구현 |
|-----------|------|
| 사용자 인증 | JWT (access + refresh), Redis 관리 |
| 소셜 로그인 | OAuth2 Authorization Code (구글/카카오) |
| 비밀번호 | BCrypt 해싱 |
| 민감정보 | AES-256-GCM 암호화 (User email) |
| 서비스 간 인증 | AES-GCM 대칭키 토큰 (30초 만료) |
| XSS 방어 | 입력값 HTML 이스케이프 + script 태그 제거 |
| 보안 헤더 | nosniff, X-XSS-Protection, DENY, CSP |
| CSRF | 불필요 (JWT 헤더 방식, 쿠키 미사용) |

---

## 8. 프론트엔드 페이지 (16개)

| 경로 | 페이지 | 기술 |
|------|--------|------|
| `/` | 홈 | MUI |
| `/login` | 로그인 (이메일 + Google/Kakao redirect) | MUI + Redux |
| `/register` | 회원가입 | MUI + Redux |
| `/oauth/callback` | OAuth2 코드 교환 | Redux |
| `/trips` | 내 여행 목록 | MUI + React Query |
| `/trips/new` | 여행 만들기 | MUI + React Query (useMutation) |
| `/trips/:id` | 여행 상세 + 예약 | MUI + React Query |
| `/bookings` | 내 예약 (결제/취소) | MUI + React Query |
| `/payments` | 결제 내역 | MUI |
| `/accommodations` | 숙소 검색 | MUI + React Query |
| `/subscription` | 구독/크레딧 관리 + 포트원 결제 | MUI + React Query + 포트원 SDK |
| `/feed` | 피드 (글+사진 업로드) | MUI + React Query + Styled Components |
| `/chat` | GPS 채팅방 찾기 | MUI + Geolocation API |
| `/chat/:roomId` | 채팅방 (실시간 메시지) | STOMP + SockJS + MUI |
| `/notifications` | 알림 (읽음/안읽음 + 배지) | MUI + React Query |
| `/streaming` | 라이브 스트리밍 (생성/시청) | MUI + hls.js |

---

## 9. DB 스키마 (10개)

| DB | 서비스 | 주요 테이블 |
|----|--------|------------|
| trip_user | user-service | users |
| trip_trip | trip-service | trips |
| trip_booking | booking-service | bookings |
| trip_payment | payment-service | payments |
| trip_accommodation | accommodation-service | accommodations |
| trip_subscription | subscription-service | subscriptions, credit_purchases |
| trip_feed | feed-service | feeds, feed_images |
| trip_media | media-service | transcoding_jobs, live_streams |
| trip_chat | chat-service | chat_rooms, chat_messages |
| trip_notification | notification-service | notifications |

모든 DB에 `outbox_events` 테이블 공통 존재 (Outbox Pattern).

---

## 10. 실행 방법

```bash
# 1. 인프라 실행
docker-compose up -d

# 2. 백엔드 빌드
./gradlew clean build -x test

# 3. 서비스 실행 (순서대로)
# Eureka Server → 각 서비스 → API Gateway

# 4. 프론트엔드
cd frontend
npm install
npm run dev

# 5. 브라우저 접속
http://localhost:5173
```

---

## 11. 남은 작업

| 항목 | 상태 | 비고 |
|------|------|------|
| WebRTC 영상통화 | 미구현 | 대규모 작업, 별도 진행 |
| TDD 테스트 | 보류 | 추후 진행 |
| E2E 실행 테스트 | 미완 | docker-compose 기동 후 전체 플로우 확인 필요 |
| CI/CD | 미구현 | GitHub Actions 등 |
| 모니터링 | 미구현 | Prometheus + Grafana |
| 로그 집중화 | 미구현 | ELK Stack |
