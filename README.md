# Ready-Hire

> AI 기반 모의면접 서비스 — 1인 풀스택 개인 프로젝트

직무 · 기술스택 · 경력을 선택하면 GPT-4o-mini가 면접 질문 5개를 생성하고, 답변에 대한 점수 · 피드백 · 모범답안을 제공합니다.  
**FREE**(하루 3회) / **PRO**(무제한, 월 9,900원) 구독 모델로 운영됩니다.

---

## Service URLs

| 서비스 | URL |
|--------|-----|
| **Frontend** | https://ready-hire-vert.vercel.app |
| **Backend API** | https://ready-hire-api-fpdhhrd5abahhxhh.koreacentral-01.azurewebsites.net |
| **Swagger UI** | https://ready-hire-api-fpdhhrd5abahhxhh.koreacentral-01.azurewebsites.net/swagger-ui/index.html |

> Azure F1 무료 플랜은 슬립 모드가 있어 첫 요청이 느릴 수 있습니다. API가 응답하지 않으면 Swagger URL을 먼저 열어 인스턴스를 깨운 뒤 이용하세요.

---

## Tech Stack

| 영역 | 기술 |
|------|------|
| **Backend** | Java 17, Spring Boot 3.3.x, Spring Security, JPA, WebFlux |
| **Frontend** | React 18, Vite, Tailwind CSS, React Router v6, Context API |
| **Database** | PostgreSQL (Supabase, Session Pooler 5432) |
| **AI** | OpenAI GPT-4o-mini (WebClient 기반) |
| **인증** | Google OAuth2 + JWT (STATELESS), Cookie 기반 인가 요청 저장 |
| **결제** | 포트원 (토스페이먼츠) |
| **배포** | Azure App Service (백엔드), Vercel (프론트엔드) |
| **CI/CD** | GitHub Actions (`main` push → 자동 배포) |
| **문서** | SpringDoc OpenAPI (Swagger) |
| **마이그레이션** | Flyway |

---

## Features

### Backend

- Google OAuth2 로그인 + JWT 발급 / 갱신 / 로그아웃
- OAuth2 STATELESS: 세션 없이 쿠키 기반 인가 요청 저장
- 면접 시작 → AI 질문 생성 → 답변 제출 → AI 피드백 → 완료 / 히스토리
- FREE 플랜 하루 3회 사용량 제한
- 포트원 결제 검증, PRO 구독 활성화 / 해지, 웹훅 엔드포인트
- 공통 응답 포맷 `ApiResponse`, `GlobalExceptionHandler`, Swagger

### Frontend

| 페이지 | 설명 |
|--------|------|
| **Login** | Google OAuth 시작 |
| **OAuth2Callback** | 토큰 저장 후 대시보드 이동 |
| **Dashboard** | 계정 · 남은 횟수 · 면접 히스토리 |
| **InterviewSetup** | 직무 · 기술스택 · 경력 설정 |
| **Interview** | 면접 진행 (질문 1개씩 순차 표시) |
| **InterviewResult** | 점수 · 피드백 결과 (PRO만 상세 제공) |
| **Subscription** | PRO 구독 · 결제 |

---

## Plan

| 기능 | FREE | PRO |
|------|------|-----|
| 면접 횟수 | 하루 **3회** | **무제한** |
| AI 피드백 | **점수만** | 점수 + 잘한 점 + 개선점 + 모범답안 |
| 가격 | 무료 | **월 9,900원** (포트원) |

---

## Architecture & Deployment

```
[Browser]
    │
    ▼
[Vercel]  ready-hire-vert.vercel.app
    │  REST API (Bearer JWT)
    ▼
[Azure App Service]  ready-hire-api
    │                    │
    │                    ├── Google OAuth2
    │                    └── OpenAI API
    ▼
[Supabase PostgreSQL]  Session Pooler :5432
```

### CI/CD

`main` 브랜치 push 시 GitHub Actions가 자동 배포합니다.

```
git push origin main
    → ./gradlew bootJar
    → azure/webapps-deploy@v2
    → Azure App Service (JAR: app.jar)
```

워크플로: [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml)

### Azure App Service (참고)

| 항목 | 값 |
|------|-----|
| Startup Command | `java -jar /home/site/wwwroot/app.jar` |
| `OAUTH2_REDIRECT_URI` | `https://ready-hire-vert.vercel.app/oauth2/callback` |
| Google Redirect URI | `{baseUrl}/login/oauth2/code/google` |

### Vercel Environment Variables

| Key | 설명 |
|-----|------|
| `VITE_API_BASE_URL` | Azure Backend API URL |
| `VITE_OAUTH2_REDIRECT_URI` | `https://ready-hire-vert.vercel.app/oauth2/callback` |
| `VITE_PORTONE_CHANNEL_KEY` | 포트원 채널 키 |

> `VITE_*` 변수는 빌드 시점에 반영됩니다. 변경 후 **Redeploy**가 필요합니다.

---

## Getting Started (Local)

### 1. 환경 변수 (`.env`)

프로젝트 루트에 `.env` 파일을 생성합니다. (`.gitignore` 포함 — 커밋 금지)

```
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_ACCESS_TOKEN_EXPIRATION_SECONDS
JWT_REFRESH_TOKEN_EXPIRATION_SECONDS
OPENAI_API_KEY
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
OAUTH2_REDIRECT_URI
PORTONE_V2_API_SECRET
PORTONE_CHANNEL_KEY
```

| 변수명 | 설명 |
|--------|------|
| `DB_URL` | PostgreSQL JDBC URL (`?sslmode=require` 권장, Session Pooler **5432**) |
| `OAUTH2_REDIRECT_URI` | 로컬: `http://localhost:5173/oauth2/callback` |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상 권장) |

**Supabase 팁:** Transaction Pooler(6543) 대신 Session Pooler(5432) 사용. Hikari `prepareThreshold: 0` 적용.

### 2. Flyway 마이그레이션

애플리케이션 기동 시 Flyway가 자동 실행됩니다. (`FlywayConfig` — 공용 DataSource)

| 파일 | 내용 |
|------|------|
| `V1__init.sql` | users, profiles, interviews 등 핵심 스키마 |
| `V2__portone_subscriptions_payments.sql` | 구독 · 결제 테이블 |

### 3. 백엔드 실행

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\gradlew.bat bootRun
```

```bash
# macOS / Linux
./gradlew bootRun
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html

### 4. 프론트엔드 실행

```bash
cd ready-hire-frontend
npm install
npm run dev
```

- App: http://localhost:5173

`ready-hire-frontend/.env.local` 예시:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_OAUTH2_REDIRECT_URI=http://localhost:5173/oauth2/callback
```

---

## API Overview

### 공통 응답 포맷

```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

인증 API는 `Authorization: Bearer <accessToken>` 헤더가 필요합니다.

### Auth — `/api/auth`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/auth/login` | 이메일 + 비밀번호 로그인 |
| `POST` | `/api/auth/refresh` | 액세스 토큰 갱신 |
| `POST` | `/api/auth/logout` | 리프레시 토큰 무효화 |

### OAuth2

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/oauth2/authorization/google` | Google 로그인 시작 |
| — | `/login/oauth2/code/google` | Google 콜백 (백엔드) |

로그인 성공 시 프론트로 리다이렉트:

```
https://ready-hire-vert.vercel.app/oauth2/callback?accessToken=...&refreshToken=...
```

### Interview — `/api/interviews`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/interviews` | 면접 시작 + 질문 5개 생성 |
| `GET` | `/api/interviews` | 면접 히스토리 목록 |
| `GET` | `/api/interviews/{id}` | 면접 상세 조회 |
| `POST` | `/api/interviews/{id}/answers` | 답변 제출 + AI 피드백 |
| `POST` | `/api/interviews/{id}/complete` | 면접 종료 |

### Payment — `/api/payments`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/payments/verify` | 포트원 결제 검증 + PRO 활성화 |
| `GET` | `/api/payments/subscription` | 구독 상태 조회 |
| `DELETE` | `/api/payments/subscription` | 구독 해지 |
| `POST` | `/api/payments/webhook` | 포트원 웹훅 |

상세 스펙: **Swagger UI** 참고

---

## Database (9 Tables)

| 테이블 | 설명 |
|--------|------|
| `users` | 회원 (plan_type: FREE / PRO) |
| `user_profiles` | 직무 / 기술스택 / 경력 |
| `subscriptions` | 구독 정보 |
| `payments` | 포트원 결제 내역 |
| `daily_usage` | FREE 일일 사용량 (하루 3회) |
| `interviews` | 면접 세션 |
| `interview_questions` | GPT 생성 질문 |
| `interview_answers` | 사용자 답변 |
| `interview_results` | AI 피드백 (PRO: 상세) |

마이그레이션 경로: `src/main/resources/db/migration/`

---

## Package Structure

```
com.devinterview.api
├── auth              # JWT, OAuth2, 인증 관련
├── domain
│   ├── interview     # 면접 세션, 질문, 답변, 결과
│   ├── payment       # 구독, 결제
│   ├── usage         # 일일 사용량
│   └── ai            # ChatService, OpenAiChatService
├── common            # ApiResponse, CustomException, ErrorCode
└── config            # SecurityConfig, WebClientConfig 등

엔트리 포인트: DevInterviewApplication
```

```
ready-hire-frontend/
├── src/
│   ├── api/          # axios + API 함수
│   ├── components/   # 공통 UI
│   ├── contexts/     # AuthContext, ToastContext
│   ├── pages/        # 라우팅 페이지
│   └── utils/        # 토큰, API unwrap
└── vercel.json
```

---

## Troubleshooting

개발·배포 과정에서 해결한 주요 이슈입니다.

| # | 증상 | 해결 |
|---|------|------|
| 1 | Flyway 마이그레이션 실패 | `V1__init.sql` UTF-16 → **UTF-8 (무 BOM)** 재저장 |
| 2 | Supabase 연결 불안정 | Transaction pool → **Session pool(5432)** + `prepareThreshold=0` |
| 3 | JPA Auditing 오류 | `OffsetDateTime` / `Instant` → **`LocalDateTime` 통일** |
| 4 | OAuth2 STATELESS 실패 | HttpSession 불가 → **쿠키 기반** 인가 요청 저장 |
| 5 | Azure 콜드 스타트 | F1 슬립 모드 → **Swagger 먼저 접속해 깨우기** |
| 6 | Google 로그인 버튼 무반응 | 상대경로 → **`VITE_API_BASE_URL` 절대경로** |

---

## License

Personal portfolio project.
