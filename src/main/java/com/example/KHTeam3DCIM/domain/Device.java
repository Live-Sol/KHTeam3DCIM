package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "DC_DEVICE") // DB 테이블명과 일치
@Getter @Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Builder // 빌더 패턴 사용 (테스트 데이터 넣을 때 편함)
@EntityListeners(AuditingEntityListener.class) // 날짜 자동 입력을 위해 필요
@SequenceGenerator(
        name = "DEVICE_SEQ_GEN",
        sequenceName = "SEQ_DEVICE_ID", // 오라클 시퀀스 이름
        initialValue = 1,
        allocationSize = 1
)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEVICE_SEQ_GEN")
    @Column(name = "DEVICE_ID")
    private Long id;

    // Rack 테이블과의 관계 설정 (N:1)
    // FetchType.LAZY: 랙 정보는 필요할 때만 조회 (성능 최적화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RACK_ID") // 외래키 컬럼명
    private Rack rack;

    // Category 테이블과의 관계 설정 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATE_ID")
    private Category category;

    @Column(name = "VENDOR", length = 50)
    private String vendor; // 제조사 (Dell, HP)

    @Column(name = "MODEL_NAME", length = 100)
    private String modelName; // 모델명

    @Column(name = "SERIAL_NUM", length = 100, unique = true, nullable = false)
    private String serialNum; // 시리얼 번호 (필수, 중복불가)

    @Column(name = "START_UNIT", nullable = false)
    private Integer startUnit; // 시작 위치 (1~42)

    @Column(name = "HEIGHT_UNIT", nullable = false)
    private Integer heightUnit; // 높이 (1U, 2U...)

    @Column(name = "STATUS", length = 20)
    private String status; // RUNNING, OFF, BROKEN

    @Column(name = "IP_ADDR", length = 50)
    private String ipAddr; // 관리 IP

    @CreatedDate // 저장될 때 시간 자동 입력
    @Column(name = "REG_DATE", updatable = false)
    private LocalDateTime regDate;

    // --- 연관관계 편의 메소드 (선택사항) ---
    // 장비를 세팅할 때 랙 정보도 같이 세팅하는 로직
//    public void setRack(Rack rack) {
//        this.rack = rack;
//        // 반대편(Rack) 리스트에도 나(Device)를 추가해준다 (객체 무결성 유지)
//        if (!rack.getDevices().contains(this)) {
//            rack.getDevices().add(this);
//        }
//    }
}