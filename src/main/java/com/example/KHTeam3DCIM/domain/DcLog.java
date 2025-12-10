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
@EntityListeners(AuditingEntityListener.class)
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
    private Long id;

    @Column(name = "MEMBER_ID", length = 50)
    private String memberId; // 누가 (Member 엔티티를 직접 참조하지 않고 ID만 남김)

    @Column(name = "TARGET_DEVICE", length = 100)
    private String targetDevice; // 대상 장비명 또는 시리얼

    @Column(name = "ACTION_TYPE", length = 20)
    private String actionType; // INSERT, UPDATE, DELETE

    @CreatedDate
    @Column(name = "LOG_DATE", updatable = false)
    private LocalDateTime logDate; // 언제
}