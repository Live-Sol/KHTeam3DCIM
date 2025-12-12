package com.example.KHTeam3DCIM.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 기록을 위해 필요
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 활동했는지 (User123, Admin 등)
    private String actor;

    // 어떤 활동이 발생했는지에 대한 간결한 설명
    @Column(length = 500)
    private String actionDescription;

    // 활동이 발생한 시간 (자동 생성)
    @CreatedDate
    private LocalDateTime timestamp;

    // 로그 타입 (예: REQUEST_APPROVE, DEVICE_CHANGE, MEMBER_JOIN 등)
    @Enumerated(EnumType.STRING)
    private LogType logType;

    public AuditLog(String actor, String actionDescription, LogType logType) {
        this.actor = actor;
        this.actionDescription = actionDescription;
        this.logType = logType;
    }
}