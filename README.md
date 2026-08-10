# Zeropick Backend

Zeropick(제로픽) 프로젝트의 백엔드 API 서버입니다.
데이터 수집·전처리·DB 적재 파이프라인은 [`ecc2team/Data`](https://github.com/ecc2team/Data) 레포에서 관리합니다.

> ⚠️ 아직 개발 초기 단계로, 모든 기능이 구현되어 있지는 않습니다.

## 소개

Zeropick은 제로/무설탕 제품의 실제 성분 안전성과 마케팅 표기 사이의 차이를 분석해 제공하는 서비스입니다.
이 레포는 해당 서비스의 REST API 서버를 담당합니다.

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5.5 (Web, Data JPA, Security, Validation) |
| Database | PostgreSQL + Flyway (마이그레이션) |
| Auth | JWT (jjwt) |
| ETC | Lombok, dotenv-java |
| Build | Gradle |

## 시작하기

### 요구 사항

- JDK 17
- PostgreSQL 접속 정보

### 환경 변수

프로젝트 루트에 `.env` 파일을 만들고 DB 접속 정보, JWT 시크릿 등 필요한 값을 채워주세요. (`dotenv-java`로 로드됩니다)

### 실행

```bash
./gradlew bootRun
```

## 폴더 구조

Spring Boot 기본 구조를 따릅니다. (개발 진행에 따라 계속 변경될 수 있습니다)
```
src/
└─ main/
├─ java/ # 애플리케이션 소스
└─ resources/ # 설정 파일, DB 마이그레이션 스크립트 등
```

## 관련 레포

- [ecc2team/Data](https://github.com/ecc2team/Data) — 데이터 수집/전처리/DB 적재 파이프라인
- [ecc2team/Frontend](https://github.com/ecc2team/Frontend) — 프론트엔드
