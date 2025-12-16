# 노후하우 인증/인가 서버 (KnowWhoHow Auth Server)

## 1. 프로젝트 개요
**KnowWhoHow Auth Server**는 마이데이터 서비스를 위한 중앙 집중식 인증(Authentication) 및 인가(Authorization) 서버입니다.

본 프로젝트는 OAuth 2.1 및 OIDC(OpenID Connect) 1.0 표준을 준수하는 **Spring Authorization Server(SAS)**를 기반으로 구축되었습니다. 단순히 ID/PW를 입력하는 방식이 아닌, 실제 금융권 마이데이터 환경과 동일하게 **금융 인증서(Financial Certificate)**를 통한 본인 확인 및 CI(Connecting Information) 기반의 사용자 식별 프로세스를 구현했습니다.

보안성과 안정성을 최우선으로 하여 모든 인프라는 온프레미스(On-Premise) 환경에 구축되었으며, 대용량 트래픽 처리를 위한 DB 이중화 및 데이터 보호를 위한 암호화 전략이 적용되었습니다.

---

## 2. 주요 기능
* **표준 OAuth 2.1 / OIDC Provider:** Authorization Code Grant 방식을 통해 클라이언트(Main Server)에게 Access Token, Refresh Token, ID Token을 안전하게 발급.
* **금융 인증서 기반 본인 확인:** 외부 금융 인증 기관 모듈(Mock)과 연동하여 전자서명 및 본인 확인 수행 후 CI 생성.
* **커스텀 동의(Consent) 처리:** 사용자가 제3자 앱(Client)에게 정보 제공을 명시적으로 허용하는 OAuth 2.0 동의 프로세스 구현.
* **DB 이중화 및 부하 분산:** Master(Write) / Slave(Read) 구조로 DB를 이중화하고 HAProxy를 통해 트래픽 부하 분산.
* **데이터 보안 (Encryption):**
    * **AES/GCM:** 민감한 개인정보(주민번호, 전화번호 등) 양방향 암호화 저장.
    * **SHA-256 / BCrypt:** 비밀번호 및 Client Secret 단방향 해시 저장.

---

## 3. 기술 스택 (Tech Stack)

### 3.1. Infrastructure (On-Premise)
| Category | Technology | Description |
| :--- | :--- | :--- |
| **Server** | Linux (Ubuntu) | 온프레미스 물리 서버 환경 |
| **CI/CD** | Jenkins | 자동화된 빌드 및 배포 파이프라인 구축 |
| **Container** | Docker, Docker Compose | 서비스 컨테이너화 및 오케스트레이션 |
| **Load Balancer** | HAProxy | DB(MySQL) 부하 분산 및 Failover 처리 |
| **Reverse Proxy** | Nginx | 웹 서버 및 리버스 프록시 |

### 3.2. Backend
* **Language:** Java 17
* **Framework:** Spring Boot 3.2
* **Security:**
    * Spring Security 6 (Core Security)
    * Spring Authorization Server 1.2 (OAuth 2.1 / OIDC Provider)
* **Database:** MySQL 8.0 (Replication 적용)
* **ORM:** Spring Data JPA
* **View Template:** Thymeleaf (커스텀 로그인 및 동의 페이지 구현)

---

## 4. 인프라 및 보안 아키텍처
본 프로젝트는 금융 데이터의 특수성을 고려하여 고가용성과 보안에 중점을 둔 아키텍처를 설계했습니다.

### 4.1. DB 이중화 및 로드 밸런싱
안정적인 인증 처리를 위해 Active-Standby 형태의 고가용성 DB 구조를 설계했습니다.
* **Master DB:** `INSERT`, `UPDATE`, `DELETE` 등 데이터 변경 트랜잭션 전담.
* **Slave DB:** `SELECT` 조회 트랜잭션 전담 (Replication).
* **HAProxy:** 애플리케이션과 DB 사이에서 트래픽을 분기하고, Master 장애 시 Slave로 자동 절체(Failover) 수행.

### 4.2. 데이터 보안 전략
* **전송 구간 암호화:** SSL/TLS 적용.
* **저장 구간 암호화:**
    * **CI, 주민등록번호:** AES-256-GCM 알고리즘을 사용하여 암호화 저장 (복호화 가능).
    * **Client Secret, Password:** BCrypt (Salted Hash) 알고리즘 적용 (복호화 불가능).

---

## 5. 디렉토리 구조
```bash
knowwhohow-auth/
├── src/
│   ├── main/
│   │   ├── java/com/knowwhohow/
│   │   │   ├── global/
│   │   │   │   ├── config/          # SecurityConfig (SAS 설정), DataInitializer
│   │   │   │   └── util/            # 암호화 유틸리티 (AES, Hash)
│   │   │   ├── domain/
│   │   │   │   ├── controller/      # AuthController (로그인), ConsentController (동의)
│   │   │   │   ├── service/         # MemberService (UserDetails), CertService (본인확인)
│   │   │   │   ├── entity/          # Member, CertificationUser, Authorization (토큰)
│   │   │   │   └── repository/      # JPA Repositories
│   │   │   └── MyDataAuthApplication.java
│   │   └── resources/
│       ├── static/              # CSS, JS (auth-flow.js 등)
│       ├── templates/           # Thymeleaf (my-cert-auth-page.html, consent.html)
│       ├── application.yml      # 서버 설정
│       └── authorization_schema.sql # SAS 표준 DB 스키마
├── docker/                          # Dockerfile
└── build.gradle

---

## 6. API 명세 (OAuth 2.0 Endpoints)
Spring Authorization Server가 제공하는 표준 엔드포인트입니다.

| Method | Endpoint | Description |
| :---: | :--- | :--- |
| GET | `/oauth2/authorize` | 인가 코드 발급 요청 (로그인 및 동의 페이지 진입) |
| POST | `/oauth2/token` | Access Token 발급 요청 (Server-to-Server) |
| POST | `/oauth2/revoke` | 토큰 폐기 요청 |
| POST | `/oauth2/introspect` | 토큰 유효성 검사 |
| GET | `/.well-known/openid-configuration` | OIDC 설정 정보 조회 (Discovery Endpoint) |
| GET | `/oauth2/jwks` | 공개키(Public Key) 조회 (리소스 서버 검증용) |

> *상세 비즈니스 로직 API는 내부 보안 정책에 따라 생략합니다.*

---

## 7. 실행 가이드 (Local Development)

### 사전 요구사항
* Docker & Docker Compose
* Java 17 (JDK)

### 실행 방법

**1. 레포지토리 클론**
```bash
git clone [https://github.com/Fisa5-Main-Project/mydata_server_authentication.git](https://github.com/Fisa5-Main-Project/mydata_server_authentication.git)
cd mydata_server_authentication```

**2. DB 컨테이너 실행 (MySQL)**
```bash
docker-compose up -d my-data-auth-db```

**3. DB 스키마 초기화**
```bash
# SAS 표준 테이블 및 Mock 데이터 생성
docker exec -i my-data-auth-db mysql -uuser -ppassword my_data_auth_db < authorization_schema.sql```

**4. 애플리케이션 빌드 및 실행**
```bash
./gradlew clean build
java -jar build/libs/knowwhohow-auth-0.0.1-SNAPSHOT.jar```
