# 🏢 StarRoot DCIM (Data Center Infrastructure Management)

> **데이터센터 자산 및 환경 통합 관리 시스템**
>
> 랙(Rack) 실장 관리, 장비 입고 승인 프로세스, 그리고 전력/냉방 환경 시뮬레이션을 제공하는 웹 애플리케이션입니다.

## 📖 프로젝트 개요 (Project Overview)
이 프로젝트는 데이터센터의 복잡한 자산과 환경을 효율적으로 운영하기 위해 개발되었습니다.
관리자는 시각화된 **랙 실장도(Rack View)**를 통해 장비 배치를 직관적으로 관리하고, **PUE(전력 사용 효율)** 및 온도를 시뮬레이션하여 최적의 운영 환경을 유지할 수 있습니다. 또한, 사용자의 장비 입고 요청부터 승인, 설치까지의 **워크플로우(Workflow)**를 체계적으로 지원합니다.

---

## 🛠 기술 스택 (Tech Stack)

### Backend
* **Java:** JDK 25
* **Framework:** Spring Boot 4.0.0
* **Database:** Oracle Database 23c (ojdbc11)
* **ORM:** Spring Data JPA
* **Template Engine:** Thymeleaf (+ Thymeleaf Extras SpringSecurity6)
* **Security:** Spring Security (Role-based Access Control)

### Frontend
* **Languages:** HTML5, CSS3, JavaScript (ES6+)
* **Framework:** Bootstrap 5
* **Library:** jQuery (Ajax 통신)

### Infra & Tools
* **Build:** Gradle
* **VCS:** Git, GitHub

---

## 🌟 주요 기능 (Key Features)

### 1. 🏢 자산 관리 (Asset Management)
* **랙(Rack) 관리:**
    * 랙 생성/수정/삭제 및 총 유닛(Unit) 관리.
    * **시각화된 실장도:** 랙 내부의 장비 배치 현황을 그래픽으로 확인 가능.
* **장비(Device) 관리:**
    * **충돌 방지 시스템:** 장비 등록 시 랙의 남은 유닛(U)과 위치 중복 여부를 자동 검증.
    * **생명주기 관리:** 장비 수정, 논리 삭제(Soft Delete) 및 영구 삭제(Hard Delete) 지원.
    * **일괄 관리:** 다중 장비 선택 후 상태 변경 및 폐기 처리.

### 2. 📝 입고 신청 프로세스 (Request Workflow)
* **사용자(User):**
    * 장비 입고 신청서 작성 (제조사, 모델명, 사이즈, 전력량 등).
    * 내 신청 현황 조회 및 승인 상태 확인.
* **관리자(Admin):**
    * 대기 중인 신청 건 조회 및 필터링.
    * **승인(Approve):** 승인과 동시에 특정 랙/위치(Unit)에 장비 자동 등록.
    * **반려(Reject):** 반려 사유 작성 및 사용자 피드백.

### 3. 🌡 환경 모니터링 및 시뮬레이션 (Environment)
* **통합 대시보드:** 전체 랙/장비 현황, 입고 대기 건수, 최근 활동 로그(Audit Log) 요약.
* **환경 시뮬레이션:**
    * 냉방 장치 설정(목표 온도, 팬 속도, 모드)에 따른 시뮬레이션 로직 탑재.
    * **PUE(Power Usage Effectiveness)** 실시간 계산 및 모니터링.

### 4. 🔐 보안 및 회원 관리 (Security & Member)
* **인증/인가:** 관리자(ADMIN)와 사용자(USER) 권한 분리.
* **회원 서비스:**
    * SMTP 기반 이메일 인증 및 비밀번호 재설정.
    * 회원가입, 정보 수정, 회원 탈퇴(Soft Delete) 기능.

---

## 📂 프로젝트 구조 (Project Structure)

```text
src/main/java/com/example/KHTeam3DCIM
├── controller      # 웹 요청 처리 (Admin, Device, Rack, Request, Member 등)
├── service         # 핵심 비즈니스 로직 및 트랜잭션 처리
├── domain          # JPA Entity (DB 테이블 매핑)
├── dto             # 데이터 전송 객체 (Request/Response DTO)
├── repository      # Data Access Layer (Spring Data JPA)
└── security        # Spring Security 설정 및 인증 로직
