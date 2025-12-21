# JoyCrew Backend Server

**JoyCrew**의 핵심 비즈니스 로직과 데이터 처리를 담당하는 백엔드 서버 저장소입니다.  
**Java 17**과 **Spring Boot 3.2**를 기반으로 **AWS EKS** 환경에서 운영되며, SaaS 서비스를 위한 **멀티테넌트(Multi-tenancy)** 아키텍처를 구현했습니다.

## System Architecture
<img width="12007" height="3197" alt="Mermaid Chart - Create complex, visual diagrams with text -2025-12-21-092817" src="https://github.com/user-attachments/assets/23cfe976-d0f5-4161-8f36-5bc896672e12" />

## Key Technical Features

### 1. Multi-tenancy & Data Isolation
* **TenantContext 패턴**: 요청 도메인(Host)을 기반으로 `TenantID`를 식별하고, 모든 DB 쿼리에 기업 식별자를 강제 적용하여 논리적 데이터 격리를 구현했습니다.
* **White-Labeling**: 기업별로 고유한 도메인(`*.joycrew.co.kr`) 접속을 지원합니다.

### 2. Concurrency Control (Pessimistic Lock)
* **비관적 락 적용**: 포인트 선물 및 상품 구매 시 발생할 수 있는 `Wallet` 잔액의 Race Condition을 방지하기 위해 `PESSIMISTIC_WRITE` 락을 적용했습니다.
* **데이터 정합성**: 금융 서비스 수준의 트랜잭션 무결성을 보장합니다.

### 3. High Availability Infrastructure (AWS EKS)
* **Auto Scaling (HPA)**: 트래픽 부하에 따라 Pod 수를 동적으로 조절하여 안정성을 확보했습니다.
* **Rolling Update**: 무중단 배포를 통해 서비스 가용성을 극대화했습니다.
* **CI/CD Pipeline**: GitHub Actions를 통해 빌드, 테스트, 이미징, EKS 배포 전 과정을 자동화했습니다.

### 4. Real-time Notification & External Integration
* **Server-Sent Events (SSE)**: 실시간 웹 알림 시스템을 구현하여 사용자 경험을 향상시켰습니다.
* **Custom Security (HMAC)**: Solapi SMS API 연동 시 HMAC-SHA256 인증을 직접 구현하여 보안성을 강화했습니다.

## Tech Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3.2.5
* **Security**: Spring Security, JWT, OAuth2
* **Database**: MySQL 8.0 (AWS RDS), H2
* **Infrastructure**: AWS EKS, EC2, S3, Docker
* **Docs**: Springdoc-OpenAPI (Swagger)

## Project Structure

```text
src/main/java/com/joycrew/backend/
├── config/             # Security, Async, WebConfig 등 설정
├── controller/         # API 엔드포인트
├── dto/                # Data Transfer Objects
├── entity/             # JPA Entities (Wallet, Employee, etc.)
├── repository/         # Data Access Layer
├── service/            # Core Business Logic
│   ├── sms/            # SMS 발송 로직 (Strategy Pattern)
│   └── ...
├── security/           # JWT, UserDetails 등 보안 로직
└── util/               # 유틸리티 클래스
