package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "DC_REQUEST")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "REQ_SEQ_GEN", sequenceName = "SEQ_REQ_ID", allocationSize = 1)

public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REQ_SEQ_GEN")
    @Column(name = "REQ_ID")
    private Long id;            // 요청 ID

    // 1. 신청자 정보
    @Column(name = "COMPANY_NAME", length = 50)
    private String companyName;    // 회사명

    @Column(name = "COMPANY_PHONE", length = 20)
    private String companyPhone;   // 회사 대표 번호

    @Column(name = "USER_NAME", nullable = false)
    private String userName;       // 담당자 성함

    @Column(name = "CONTACT")
    private String contact;       // 담당자 직통 번호

    // 2. 장비 정보
    @Column(name = "CATE_ID")
    private String cateId;      // 장비 종류

    @Column(name = "VENDOR")
    private String vendor;      // 제조사

    @Column(name = "MODEL_NAME")
    private String modelName;   // 모델명

    @Column(name = "HEIGHT_UNIT", nullable = false)
    private Integer heightUnit; // 2U, 4U, 6U 등

    // ⭐ [추가] 신청 단계에서도 전력량과 EMS 여부를 받습니다.
    @Column(name = "POWER_WATT")
    private Integer powerWatt;      // 예상 소비 전력

    @Column(name = "EMS_STATUS", length = 10)
    private String emsStatus;       // EMS 사용 신청 (ON/OFF)

    // 3. 상태 및 계약 정보
    @Column(name = "STATUS")
    private String status;      // WAITING, APPROVED, REJECTED

    @CreatedDate
    @Column(name = "REQ_DATE", updatable = false)
    private LocalDateTime reqDate;  // 요청일자

    @Column(name = "PURPOSE", length = 200)
    private String purpose; // 입주 목적

    @Column(name = "START_DATE")
    private LocalDate startDate; // 입주 희망 시작일

    @Column(name = "TERM_MONTH")
    private Integer termMonth; // 계약 기간

    // 이 신청을 작성한 사용자 ID
    @Column(name = "MEMBER_ID", nullable = false, columnDefinition = "VARCHAR2(255) DEFAULT 'SYSTEM'")
    private String memberId;

    // 반려 사유
    @Column(name = "REJECT_REASON", length = 300)
    private String rejectReason;

    // 승인 이력 숨김
    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false; // 기본값 false

    // 승인 완료 시 부여된 장비의 시리얼 번호를 저장
    @Column(name = "SERIAL_NUM", length = 100)
    private String serialNum;

    // 관리자 삭제 사유 필드 (DB 컬럼 추가 필요)
    @Column(length = 500)
    private String deleteReason;
}