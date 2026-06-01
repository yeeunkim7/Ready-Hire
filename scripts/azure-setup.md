# Azure App Service 배포 가이드 (Ready-Hire 백엔드)

Spring Boot JAR를 Azure App Service에 올리고, GitHub Actions(`azure/webapps-deploy@v3`)로 `main` 브랜치 push 시 자동 배포합니다.

---

## 1. Azure CLI 설치

### Windows (winget)

```powershell
winget install Microsoft.AzureCLI
```

### macOS

```bash
brew install azure-cli
```

### 로그인

```bash
az login
az account set --subscription "<구독 ID 또는 이름>"
```

---

## 2. 리소스 그룹 생성

```bash
az group create \
  --name rg-ready-hire \
  --location koreacentral
```

---

## 3. App Service Plan 생성

| SKU | 설명 |
|-----|------|
| **F1** | 무료 (개발/테스트용, 제한 있음) |
| **B1** | Basic 유료 (소규모 프로덕션) |

무료로 시작할 때:

```bash
az appservice plan create \
  --name plan-ready-hire \
  --resource-group rg-ready-hire \
  --sku F1 \
  --is-linux
```

Basic(B1) Linux 플랜 예시:

```bash
az appservice plan create \
  --name plan-ready-hire \
  --resource-group rg-ready-hire \
  --sku B1 \
  --is-linux
```

---

## 4. Web App 생성 (Java 17)

앱 이름은 전역 고유해야 합니다 (`ready-hire-api-<랜덤>` 등).

```bash
az webapp create \
  --name ready-hire-api \
  --resource-group rg-ready-hire \
  --plan plan-ready-hire \
  --runtime "JAVA:17-java17"
```

Spring Boot 실행 명령 (JAR 배포 시):

```bash
az webapp config set \
  --name ready-hire-api \
  --resource-group rg-ready-hire \
  --startup-file "java -jar /home/site/wwwroot/*.jar"
```

또는 포털: **Configuration** → **General settings** → **Startup Command**에 동일하게 입력.

---

## 5. 애플리케이션 설정 (환경 변수)

Supabase·JWT·OAuth·PortOne 등은 **App Service → Configuration → Application settings**에 추가합니다.  
이름은 로컬 `.env` / `application.yml`과 동일하게 맞춥니다.

| 설정 이름 | 설명 |
|-----------|------|
| `DB_URL` | Supabase Session Pooler JDBC URL (`jdbc:postgresql://...:5432/postgres?sslmode=require`) |
| `DB_USERNAME` | DB 사용자 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET` | JWT 서명 키 (32바이트 이상) |
| `JWT_ACCESS_TOKEN_EXPIRATION_SECONDS` | 예: `3600` |
| `JWT_REFRESH_TOKEN_EXPIRATION_SECONDS` | 예: `1209600` |
| `OPENAI_API_KEY` | OpenAI API 키 |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 시크릿 |
| `OAUTH2_REDIRECT_URI` | OAuth 리다이렉트 URI (프론트/백엔드와 일치) |
| `PORTONE_V2_API_SECRET` | 포트원 API 시크릿 |
| `PORTONE_CHANNEL_KEY` | 포트원 채널 키 |

CLI 예시:

```bash
az webapp config appsettings set \
  --name ready-hire-api \
  --resource-group rg-ready-hire \
  --settings \
    DB_URL="jdbc:postgresql://..." \
    DB_USERNAME="..." \
    DB_PASSWORD="..." \
    JWT_SECRET="..." \
    OPENAI_API_KEY="..."
```

저장 후 앱이 재시작됩니다.

---

## 6. GitHub Actions Secrets

**GitHub** → 레포지토리 → **Settings** → **Secrets and variables** → **Actions**

### 추가

| Secret | 값 |
|--------|-----|
| `AZURE_APP_NAME` | Web App 이름 (예: `ready-hire-api`) |
| `AZURE_PUBLISH_PROFILE` | Azure Portal → Web App → **Get publish profile** 다운로드 XML **전체 내용** |

### 제거 (EC2용, 더 이상 사용 안 함)

- `EC2_HOST`
- `EC2_SSH_KEY`

### 유지 (빌드/런타임에 필요 시)

DB·JWT·OAuth·PortOne 등은 **Azure Application settings**에 두는 것을 권장합니다.  
Gradle 빌드 단계에서 시크릿이 필요하지 않으면 GitHub Secrets에 DB 관련 항목을 둘 필요는 없습니다.

---

## 7. Publish Profile 가져오기

1. [Azure Portal](https://portal.azure.com) → **App Services** → 해당 Web App
2. **Overview** → **Download publish profile**
3. XML 파일을 열어 **전체**를 복사해 `AZURE_PUBLISH_PROFILE` Secret에 붙여넣기

---

## 8. 배포 확인

`main` 브랜치에 백엔드 변경 push → **Actions** 탭에서 **Deploy to Azure App Service** 워크플로 성공 여부 확인.

```bash
curl https://<AZURE_APP_NAME>.azurewebsites.net/actuator/health
# 또는 Swagger: https://<AZURE_APP_NAME>.azurewebsites.net/swagger-ui.html
```

(헬스 엔드포인트가 없으면 공개 API 경로로 확인)

---

## 참고

- 프론트엔드는 Vercel 등 별도 배포. `VITE_API_BASE_URL`을 `https://<AZURE_APP_NAME>.azurewebsites.net`으로 설정.
- `Dockerfile` / `docker-compose.yml`은 Azure JAR 배포 경로에서는 사용하지 않습니다.
- EC2 초기 세팅은 `scripts/ec2-setup.sh` (레거시, Azure 미사용).
