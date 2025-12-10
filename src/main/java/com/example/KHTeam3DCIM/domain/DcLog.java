// DcLog.java (작업 이력서 / CCTV)
// 파일 설명: "블랙박스"입니다. 누가 장비를 등록했는지, 수정했는지 기록을 남깁니다.

package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "DC_LOG")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 시간 자동 기록을 위해 필수
@SequenceGenerator(
        name = "LOG_SEQ_GEN",
        sequenceName = "SEQ_LOG_ID",
        initialValue = 1,
        allocationSize = 1
)
public class DcLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_SEQ_GEN")
    @Column(name = "LOG_ID")
    private Long id; // 로그 번호 1, 2, 3...

    // [1] 관계를 끊고 단순히 문자열(String)로 저장합니다.
    @Column(name = "MEMBER_ID", length = 50)
    private String memberId; // 누가? (예: "admin")

    @Column(name = "TARGET_DEVICE", length = 100)
    private String targetDevice; // 무엇을? (예: "SN-123456")

    @Column(name = "ACTION_TYPE", length = 20)
    private String actionType; // 무엇을 했나? (예: "INSERT", "DELETE")

    @CreatedDate
    @Column(name = "LOG_DATE", updatable = false)
    private LocalDateTime logDate; // 언제? (자동입력)
}