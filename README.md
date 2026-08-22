# 🥤 ZeroPick

> **성분을 알면 바뀌는 선택.**
> 제로/무설탕 식품의 성분을 분석하고 비교해주는 서비스, ZeroPick의 백엔드 레포지토리입니다.

<br>

## 👥 팀원

<div align="center">

| **김미리** (Backend Lead) | **김아인** | 
| :---: | :---: | 
| [<img src="https://avatars.githubusercontent.com/mirikim404?v=4" height=120 width=120><br/>@mirikim404](https://github.com/mirikim404) | [<img src="https://avatars.githubusercontent.com/clairekim0012-creator?v=4" height=120 width=120><br/>@clairekim0012-creator](https://github.com/clairekim0012-creator)|

</div>


<br>

## 🛠 기술 스택

| 구분 | 스택 |
| :--- | :--- |
| Language / Framework | Java 17, Spring Boot 3.5.5 |
| Database | PostgreSQL (Supabase, Session Pooler) |
| ORM / Migration | Spring Data JPA, Flyway |
| 인증 | Spring Security, JWT(jjwt), HttpOnly 쿠키 기반 인증 |
| 소셜 로그인 | Kakao / Google OAuth2 (RestClient 기반 REST API 연동) |
| 이메일 발송 | Resend, Mailgun |
| API 문서 | springdoc-openapi (Swagger UI) |
| 환경변수 | dotenv-java |
| 공통 | Lombok |
| 배포 | Render |

<br>

## ✨ 주요 기능

- **회원 인증**: 이메일/비밀번호 회원가입(이메일 인증코드 검증 필수), 로그인, 카카오·구글 소셜 로그인(첫 로그인 여부 `isNewUser` 응답)
- **토큰 관리**: JWT 액세스 토큰 발급 + HttpOnly 쿠키 기반 리프레시 토큰, 재발급 시 토큰 로테이션
- **제품/성분 조회**: 제품 상세, 성분 상세, 최근 본 제품 목록
- **비교함**: 제품을 비교함에 담고 나란히 비교
- **섭취 기록**: 하루 섭취량 기록 및 조회
- **온보딩**: 회원가입 시 선호 카테고리 · 비선호 성분 · 알레르기 정보 저장

<br>

## 📂 프로젝트 구조

기능(도메인) 단위로 패키지를 나누는 package-by-feature 구조를 따릅니다.

```
backend
├── auth               # 회원가입/로그인/소셜로그인/토큰 재발급
│   ├── oauth          # Kakao/Google OAuth 클라이언트
│   └── dto
├── user               # 회원 정보, 탈퇴, 계정 찾기
├── email              # 이메일 인증코드 발송/검증
├── product            # 제품 상세, 최근 본 제품
├── ingredient         # 성분 상세
├── comparison         # 제품 비교함
├── intake             # 하루 섭취량 기록
├── category           # 카테고리
└── global
    ├── security       # JWT, 쿠키, Spring Security 설정
    ├── exception      # 전역 예외 처리
    └── response        # 공통 응답 포맷(ApiResponse)
```

각 도메인은 필요에 따라 `controller / service / repository / entity / dto`로 세분화합니다.

<br>

## 📑 API 문서

로컬 실행 후 Swagger UI에서 전체 API 명세를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

<br>

## 🌱 브랜치 전략

가벼운 기능 브랜치 방식을 사용합니다.

| 브랜치 | 용도 |
| :--- | :--- |
| `main` | 배포 브랜치. `dev`에서 검증된 내용만 병합 |
| `dev` | 개발 통합 브랜치. 모든 기능 브랜치는 여기로 병합 |
| `feature/#이슈번호-내용` | 기능 개발 브랜치 |
| `fix/#이슈번호-내용` | 버그 수정 브랜치 |

> 예시: `feature/#24-kakao-social-login`

<br>

## 📝 커밋 컨벤션

`<type>: <설명> (#이슈번호)` 형식을 따릅니다.

| Type | 설명 | 예시 |
| :--- | :--- | :--- |
| `feat` | 기능 추가 | `feat: 카카오 소셜 로그인 추가 (#24)` |
| `fix` | 버그 수정 | `fix: refreshToken 재발급 오류 수정 (#31)` |
| `refactor` | 리팩터링 | `refactor: AuthService 소셜로그인 로직 분리 (#33)` |
| `chore` | 설정/의존성 등 기타 작업 | `chore: application.properties 환경변수 정리 (#35)` |
| `docs` | 문서 수정 | `docs: README 업데이트 (#40)` |
| `test` | 테스트 코드 | `test: AuthService 소셜로그인 테스트 추가 (#36)` |
| `del` | 파일/코드 삭제 | `del: 사용하지 않는 LogoutRequest 삭제 (#37)` |

<br>

## 🔀 PR 컨벤션

```
## 작업 내용
-

## 관련 이슈
- close #이슈번호

## 확인해줬으면 하는 부분


```

- 리뷰어 1명 이상 승인 후 병합
- 병합 전 `dev` 최신 상태로 로컬에서 충돌 확인

<br>

## 🧭 코드 컨벤션

- **DTO는 record로 작성** (요청/응답 모두)
- **Lombok**: `@Getter`, `@RequiredArgsConstructor`, `@Builder` 위주 사용, 엔티티에 `@Setter` 대신 의미 있는 메서드 정의 (예: `updateRefreshToken()`, `withdraw()`)
- **삭제는 소프트 삭제** (`deletedAt` 컬럼, 실제 DELETE 지양)
- **응답 포맷 통일**: 모든 API는 `ApiResponse<T>` (status, message, data)로 감싸서 반환
- **예외 처리는 `GlobalExceptionHandler`에서 일괄 처리**: `IllegalArgumentException` → 400, `IllegalStateException` → 409, `UnauthorizedException` → 401
- **주석은 한글로**, 로직 흐름이 복잡한 메서드는 단계별로 번호 주석 작성

<br>

## ⚙️ 환경변수

`.env` 파일에 아래 값이 필요합니다. (`.gitignore`에 포함되어 있으니 직접 공유받아 설정)

```
# DB (Supabase)
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# JWT
JWT_SECRET=

# 쿠키
APP_COOKIE_SECURE=
APP_COOKIE_SAME_SITE=
APP_COOKIE_DOMAIN=

# 이메일
RESEND_API_KEY=
MAILGUN_DOMAIN=
MAILGUN_API_KEY=

# 소셜 로그인
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=
```

<br>

## 🚀 로컬 실행

```bash
git clone https://github.com/ecc2team/Backend.git
cd Backend

# .env 파일 생성 후 위 환경변수 채우기

./gradlew bootRun
```

<br>

## 🗂 ERD

<img width="2018" height="1551" alt="image" src="https://github.com/user-attachments/assets/29e6cff2-8e1c-4179-b0f7-081c96ae4073" />
