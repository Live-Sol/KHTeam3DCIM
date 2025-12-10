// Device.java (장비 설계도)
// 파일 설명: "서버 주민등록증"입니다. 실제 데이터센터에 있는 물리적인 서버 하나하나의 정보를 담고 있습니다.

package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
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
        name = "DEVICE_SEQ_GEN",             // 자바에서 부를 별명
        sequenceName = "SEQ_DEVICE_ID",      // 실제 오라클 DB에 있는 시퀀스 이름
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
    @Column(name = "REG_DATE", updatable = false)
    private LocalDateTime regDate;

    // [7] 연관관계 편의 메소드: "장비를 랙에 꽂으면, 랙 입장에서도 장비 목록에 이게 추가되어야 해"
    // 양쪽의 데이터를 맞춰주는 꼼꼼한 코드입니다. (나중에 이해해도 됩니다)
//    public void setRack(Rack rack) {
//        this.rack = rack;
//        if (!rack.getDevices().contains(this)) {
//            rack.getDevices().add(this);
//        }
//    }
}