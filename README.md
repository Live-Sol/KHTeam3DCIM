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
src/main/java
 └── com.example.KHTeam3DCIM
      │
      ├── 📂 config
      │    ├── FilterConfig.java
      │    ├── GlobalDataAdvice.java
      │    ├── SecurityConfig.java
      │    └── WebConfig.java   
      │    
      ├── 📂 controller 
      │    ├── AdminController.java  
      │    ├── DeviceController.java 
      │    ├── EnvironmentController.java
      │    ├── infoController.java
      │    ├── MainController.java    
      │    ├── MemberController.java 
      │    ├── RackController.java    
      │    ├── RequestController.java    
      │    ├── SolutionController.java
      │    └── SpecController.java
      │        
      ├── 📂 domain
      │    ├── AuditLog.java  
      │    ├── Category.java      
      │    ├── DcimEnvironment.java
      │    ├── Device.java  
      │    ├── LogType.java         
      │    ├── Member.java      
      │    ├── Rack.java        
      │    ├── Request.java   
      │    └── Role.java       
      │
      ├── 📂 dto           
      │    ├── 📂 admin
      │    │    ├── MemberAdminResponse.java
      │    │    ├── MemberAdminUpdateRequest.java
      │    │    └── MemberFindByIdAdmin.java
      │    ├── 📂 device
      │    │    ├── deviceDTO.java
      │    │    └── DeviceResponse.java
      │    ├── 📂 Member
      │    │    ├── MemberAdminResponse.java
      │    │    ├── MemberAdminUpdateRequest.java
      │    │    ├── MemberCreateRequest.java
      │    │    ├── MemberLoginRequest.java
      │    │    ├── MemberResponse.java
      │    │    └── MemberUpdateRequest.java
      │    ├── 📂 Rack
      │    │    ├── RackCreateRequest.java
      │    │    ├── RackDetailDto.java
      │    │    ├── RackResponse.java
      │    │    └── RackUpdateRequest.java
      │    └── 📂 Request
      │         └── RequestDTO.java
      │
      ├── 📂 filter
      │    └── LogFilter.java
      │
      ├── 📂 repository
      │    ├── AuditLogRepository.java
      │    ├── CategoryRepository.java
      │    ├── DcimEnvironmentRepository.java
      │    ├── DeviceRepository.java      
      │    ├── MemberRepository.java
      │    ├── RackRepository.java
      │    └── RequestRepository.java      
      │
      ├── 📂 service
      │    ├── AdminService.java
      │    ├── AuditLogService.java
      │    ├── CategoryService.java 
      │    ├── CustomUserDetailsService.java
      │    ├── DeviceService.java
      │    ├── EnvironmentService.java
      │    ├── MailService.java
      │    ├── MemberService.java  
      │    ├── RackService.java
      │    └── RequestService.java
      │
      ├── KhTeam3DcimApplication.java
      └── DeviceScheduler.java

src/main/resources
 ├── application.properties    (DB 접속 정보 설정)
 │
 ├── 📂 static              
 │    ├── 📂css
 │    │    ├── memberStyle.css
 │    │    └── rack_view.css

 │    └── 📂js
 │         ├── device_form.js
 │         └── rack_view.js 
 │
 └── 📂 templates
      ├── 📂 device
      │    ├── device_list.html      
      │    └── device_form.html       
      │      
      ├── 📂 fragments        
      │    ├── header.html    
      │    └── layout.html   
      │
      ├── 📂 member
      │    ├── adminEditMember.html      
      │    ├── deleteMember.html
      │    ├── editMember.html
      │    ├── findMemberById.html
      │    ├── findMembersAdmin.html
      │    ├── findMembersUser.html
      │    ├── login.html 
      │    └── signup.html
      │
      ├── 📂 rack
      │    ├── rack_list.html       
      │    ├── rack_form.html       
      │    └── rack_view.html      
      │
      ├── 📂 request
      │    ├── RequestForm.html
      │    └── RequestList.html
      ├── admin.html      
      └── index.html      
---

## 📊 Entity Relationship Diagram (ERD)
> 프로젝트의 실제 데이터베이스 구조를 반영한 ERD입니다.

```mermaid
erDiagram
    %% 1. 핵심 관계 (Key Relationships)
    MEMBER ||--o{ DEVICE : "소유 (1:N)"
    MEMBER ||--o{ REQUEST : "신청 (1:N)"
    RACK ||--o{ DEVICE : "설치 (1:N)"
    CATEGORY ||--o{ DEVICE : "분류 (1:N)"
    CATEGORY ||--o{ REQUEST : "분류 (1:N)"

    %% 2. Entity Attributes (엔티티 속성)
    MEMBER {
        String memberId PK
        String name
        String role "ADMIN/USER"
    }
    RACK {
        Long id PK
        String rackName
        Long totalUnit
    }
    DEVICE {
        Long id PK
        Long rack_id FK
        String cate_id FK
        String member_id FK
        String serialNum
        String status
    }
    REQUEST {
        Long id PK
        String memberId FK
        String cateId FK
        String status
    }
    CATEGORY {
        String id PK "SVR, NET..."
        String name
    }
    DC_ENVIRONMENT {
        Long id PK
        Double currentPue
        Double currentTemp
    }
    AUDIT_LOG {
        Long id PK
        String actor
        String actionDescription
    }
