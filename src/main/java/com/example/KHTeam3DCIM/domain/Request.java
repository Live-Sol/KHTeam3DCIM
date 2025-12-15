// 장비 요청

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

    // 1. 신청자 정보 (상세화)
    @Column(name = "COMPANY_NAME", length = 50)
    private String companyName;    // 회사명 (⭐추가됨)

    @Column(name = "COMPANY_PHONE", length = 20)
    private String companyPhone;   // 회사 대표 번호 (⭐추가됨)

    @Column(name = "USER_NAME", nullable = false)
    private String userName;       // 담당자 성함 (기존 필드 활용)

    @Column(name = "CONTACT")
    private String contact;       // 담당자 직통 번호 (기존 필드 활용)

    @Column(name = "CATE_ID")
    private String cateId;      // 장비 종류 (Category ID)

    @Column(name = "VENDOR")
    private String vendor;      // 제조사

    @Column(name = "MODEL_NAME")
    private String modelName;   // 모델명

    @Column(name = "HEIGHT_UNIT", nullable = false)
    private Integer heightUnit; // 2U, 4U, 6U 등

    @Column(name = "STATUS")
    private String status;      // WAITING, APPROVED

    @CreatedDate
    @Column(name = "REQ_DATE", updatable = false)
    private LocalDateTime reqDate;  // 요청일자

    @Column(name = "PURPOSE", length = 200)
    private String purpose; // 입주 목적 (예: 웹 서비스 운영, DB 백업용, 사내 테스트 서버 등)

    @Column(name = "START_DATE")
    private LocalDate startDate; // 입주 희망 시작일 (언제부터 쓸 건지)

    @Column(name = "TERM_MONTH")
    private Integer termMonth; // 계약 기간 (개월 수 단위: 12, 24, 36...)

}
