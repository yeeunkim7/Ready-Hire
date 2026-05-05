# Ready-Hire

> AI 기반 모의면접 서비스 — 1인 풀스택 개인 프로젝트

직무 · 기술스택 · 경력을 선택하면 GPT-4o-mini가 면접 질문 5개를 생성하고, 답변에 대한 점수 · 피드백 · 모범답안을 제공합니다.
무료 플랜(하루 3회) / PRO 플랜(무제한) 구독 모델로 운영됩니다.

---

## Tech Stack

| 영역 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.3.x, Spring Security, JPA, WebFlux |
| Frontend | React, Tailwind CSS |
| Database | PostgreSQL (Supabase) |
| AI | OpenAI GPT-4o-mini (WebClient 기반) |
| 인증 | Google OAuth2 + JWT (STATELESS), jjwt 0.12.x |
| 결제 | 포트원 (토스페이먼츠) |
| 배포 | AWS EC2 (백엔드), Vercel (프론트엔드) |
| 문서 | SpringDoc OpenAPI (Swagger) |
| 마이그레이션 | Flyway |

---

## Getting Started

### 1. 환경 변수 설정

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `DB_URL` | PostgreSQL URL (예: `jdbc:postgresql://localhost:5432/readyhire`) | — |
| `DB_USERNAME` | DB 사용자명 | — |
| `DB_PASSWORD` | DB 비밀번호 | — |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상 권장) | — |
| `JWT_ACCESS_TOKEN_EXPIRATION_SECONDS` | 액세스 토큰 만료 시간 | `3600` |
| `JWT_REFRESH_TOKEN_EXPIRATION_SECONDS` | 리프레시 토큰 만료 시간 | `1209600` |
| `OPENAI_API_KEY` | OpenAI API 키 | — |
| `GOOGLE_CLIENT_ID` | Google OAuth2 클라이언트 ID | — |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 클라이언트 시크릿 | — |

### 2. DB 마이그레이션

애플리케이션 시작 시 Flyway가 자동으로 `V1__init.sql`을 실행합니다. 별도 작업이 필요 없습니다.

### 3. 실행

```bash
# macOS / Linux
./gradlew bootRun

# Windows PowerShell
.\gradlew.bat bootRun
```

---

## API

### 공통 응답 포맷

모든 API 응답(JWT 인증 필터 오류 포함)은 아래 구조를 따릅니다.

```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

인증이 필요한 엔드포인트는 `Authorization: Bearer <accessToken>` 헤더를 포함해야 합니다.

---

### Auth — `/api/auth`

#### POST `/api/auth/login`

이메일 + 비밀번호 로그인

```json
// Request
{
  "email": "user@example.com",
  "password": "plainPassword"
}

// Response 200
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

#### POST `/api/auth/refresh`

액세스 토큰 갱신

```json
// Request
{
  "refreshToken": "eyJ..."
}

// Response 200
{
  "success": true,
  "message": "Token refreshed.",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

#### POST `/api/auth/logout`

리프레시 토큰 무효화

```json
// Request
{
  "refreshToken": "eyJ..."
}

// Response 200
{
  "success": true,
  "message": "Logged out successfully.",
  "data": null
}
```

---

### Interview — `/api/interviews` _(구현 예정)_

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/interviews` | 면접 시작 + 질문 5개 생성 |
| `POST` | `/api/interviews/{id}/answers` | 답변 제출 + AI 피드백 생성 |
| `GET` | `/api/interviews` | 면접 히스토리 목록 조회 |
| `GET` | `/api/interviews/{id}` | 면접 상세 조회 |

---

## DB 설계 (9개 테이블)

```
users               — 회원 기본 정보 (plan_type 컬럼으로 플랜 관리)
user_profiles       — 직무 / 기술스택 / 경력 프로필
subscriptions       — 구독 정보 (FREE / PRO), user당 active 구독 1개 제한
payments            — 포트원 결제 내역
daily_usage         — 무료 플랜 일일 사용량 (하루 3회 제한)
interviews          — 면접 세션
interview_questions — GPT 생성 질문
interview_answers   — 사용자 답변
interview_results   — AI 피드백 (detailed_feedback: jsonb, PRO 플랜만 저장)
```

마이그레이션 파일: `src/main/resources/db/migration/V1__init.sql`

---

## 플랜 구분

| 기능 | FREE | PRO |
|------|------|-----|
| 면접 횟수 | 하루 3회 | 무제한 |
| AI 피드백 | 기본 점수 | 점수 + 잘한점 + 개선점 + 모범답안 |
| 히스토리 | 최근 10건 | 전체 |
| 결제 | — | 포트원 (토스페이먼츠) |

---

## 개발 현황

| 주차 | 작업 | 상태 |
|------|------|------|
| 1주 | 프로젝트 세팅, ERD, JWT 인증 | ✅ 완료 |
| 2주 | Google OAuth2, 회원가입/조회 API | 🔄 진행 중 |
| 3주 | 면접 시작 / 질문 생성 + OpenAI 연동 | ⏳ 예정 |
| 4주 | 답변 저장 / 면접 종료 / 피드백 생성 | ⏳ 예정 |
| 5주 | 사용량 제한 + 포트원 결제 연동 | ⏳ 예정 |
| 6주 | React 프론트엔드 | ⏳ 예정 |
| 7주 | 테스트 코드 + AWS 배포 | ⏳ 예정 |
| 8주 | 앱 래핑 + 스토어 제출 | ⏳ 예정 |

---

## API 문서

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Package Structure

```
com.devinterview.api
├── auth          # JWT, OAuth2, 인증 관련
├── user          # 회원, 구독, 결제
├── interview     # 면접 세션, 질문, 답변, 결과
├── ai            # ChatService, OpenAiChatService
├── common        # ApiResponse, CustomException, ErrorCode
└── config        # SecurityConfig, WebClientConfig 등
```

> 엔트리 포인트: `DevInterviewApplication`