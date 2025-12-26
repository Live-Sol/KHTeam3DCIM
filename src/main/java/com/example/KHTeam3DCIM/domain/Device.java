package com.example.KHTeam3DCIM.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

// [1] @Entity: "이 클래스는 자바 객체가 아니라, DB 테이블 그 자체야!"라고 선언하는 것입니다.
@Entity
// [2] @Table: "DB에 DC_DEVICE라는 이름으로 테이블을 만들어라"라는 뜻입니다.
@Table(name = "DC_DEVICE")
@Getter @Setter // 변수마다 getStartUnit(), setStartUnit() 등을 자동으로 만들어줍니다.
@NoArgsConstructor // 텅 빈 장비 객체(new Device())를 만들 수 있게 해줍니다.
@AllArgsConstructor // 꽉 찬 장비 객체를 만들 수 있게 해줍니다.
@Builder // 장비 정보를 입력할 때 순서 헷갈리지 않게 도와주는 도구입니다.
@EntityListeners(AuditingEntityListener.class) // "누가 언제 등록했는지 감시해라" (날짜 자동입력용)
// [3] @SequenceGenerator: 오라클은 번호표 기계(시퀀스)가 따로 필요해서 설정하는 부분입니다.
@SequenceGenerator(
        name = "DEVICE_SEQ_GEN",      // 자바에서 부를 별명
        sequenceName = "SEQ_DEVICE_ID", // 실제 오라클 DB에 있는 시퀀스 이름
        initialValue = 1, allocationSize = 1 // 1번부터 시작하고 1씩 증가한다.
)
public class Device {

    @Id // "이게 바로 주민등록번호(Primary Key)야"
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEVICE_SEQ_GEN") // "번호는 내가 안 정하고 시퀀스 기계한테 뽑아올게"
    @Column(name = "DEVICE_ID") // DB 컬럼 이름 지정
    private Long id; // 장비 고유 ID

    // ==========================================
    // ⭐ 가장 중요한 부분: 연관관계 매핑 (Foreign Key)
    // ==========================================

    // [4] @ManyToOne: "장비(Many)는 랙(One) 하나에 속한다" (N:1 관계)
    // FetchType.LAZY: "장비 정보 가져올 때, 랙 정보는 나중에 필요할 때 가져와" (성능 최적화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RACK_ID") // "DB에는 RACK_ID라는 이름으로 상대방 ID를 저장할게"
    private Rack rack; // 이제 자바에서는 ID 숫자 대신 'Rack 객체' 자체를 넣으면 됩니다.

    // [5] "장비(Many)는 하나의 카테고리(One)에 속한다"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATE_ID")
    private Category category;

    // ==========================================
    // 일반 정보들 (DB 컬럼들)
    // ==========================================

    @Column(name = "VENDOR", length = 50)
    private String vendor; // 제조사 (예: Dell, HP)

    @Column(name = "MODEL_NAME", length = 100)
    private String modelName; // 모델명 (예: PowerEdge R740)

    @Column(name = "SERIAL_NUM", length = 100, unique = true, nullable = false)
    private String serialNum; // 시리얼 번호 (unique=true: 중복되면 에러 발생!)

    @Column(name = "START_UNIT", nullable = false)
    private Integer startUnit; // 랙의 몇 번째 칸에 꽂혀있는지 (예: 10)

    @Column(name = "HEIGHT_UNIT", nullable = false)
    private Integer heightUnit; // 크기가 몇 칸짜리인지 (예: 2U)

    @Column(name = "STATUS", length = 20)
    private String status; // 상태 (RUNNING=가동중, OFF=꺼짐)

    @Column(name = "IP_ADDR", length = 50)
    private String ipAddr; // 관리용 IP 주소

    // [6] @CreatedDate: 저장(save)할 때 현재 시간을 자동으로 채워줍니다. (개발자가 안 넣어도 됨)
    @CreatedDate
    @Column(name = "REG_DATE", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Seoul") // JSON 응답용
    @DateTimeFormat(pattern = "yyyy/MM/dd") // 폼 데이터 바인딩용
    private LocalDateTime regDate;

    // ==========================================
    // 계약 및 입고 정보
    // ==========================================
    @Column(name = "CONTRACT_DATE")
    private LocalDate contractDate; // 입고(계약) 시작일

    @Column(name = "CONTRACT_MONTH")
    private Integer contractMonth;  // 계약 기간 (개월 수)

    // ==========================================
    // [핵심] 장비 소유자 (회원과 연결), Member 객체 자체를 연결합니다.
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID") // DC_DEVICE 테이블에 MEMBER_ID 컬럼이 생김
    private Member member;

    // ==========================================
    // 소유자 및 용도 정보 (신청서 연동용)
    // ==========================================
    @Column(name = "COMPANY_NAME", length = 50)
    private String companyName;     // 회사명
    @Column(name = "COMPANY_PHONE", length = 20)
    private String companyPhone;    // 회사 대표 번호
    @Column(name = "USER_NAME", length = 50)
    private String userName;        // 담당자명
    @Column(name = "CONTACT", length = 50)
    private String contact;         // 연락처 (담당자 직통 번호)
    @Column(name = "DESCRIPTION", length = 500)
    private String description;     // 용도 및 설명
    @Column(name = "POWER_WATT")
    private Integer powerWatt;      // 소비 전력 (W)
    @Column(name = "EMS_STATUS", length = 10)
    private String emsStatus;       // EMS 감시 상태 (ON/OFF)
    // 관리자 삭제 사유 필드 (DB 컬럼 추가 필요)
    private String deleteReason;
    // 삭제된 시점 기록
    private LocalDateTime deletedAt;

    // 마감일 계산 (contractDate: 시작일, contractMonth: 기간)
    public LocalDate getEndDate() {
        if (this.contractDate == null || this.contractMonth == null) return null;
        return this.contractDate.plusMonths(this.contractMonth);
    }

    // 만료 여부 확인
    public boolean getIsExpired() {
        LocalDate endDate = getEndDate();
        if (endDate == null) return false;
        return endDate.isBefore(LocalDate.now());
    }



}