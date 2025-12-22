package com.example.KHTeam3DCIM.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DC_ENVIRONMENT")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DcimEnvironment {

    // 시스템 설정은 딱 1개의 행(Row)만 가집니다. (ID = 1 고정)
    @Id
    @Column(name = "ENV_ID")
    private Long id;

    // --- 관리자가 조절 가능한 값 (Cooling Page) ---
    @Column(name = "TARGET_TEMP")
    private Double targetTemp;      // 목표 실내 온도 (예: 24.0도)

    @Column(name = "FAN_SPEED")
    private Integer fanSpeed;       // 냉각 팬 속도 (0 ~ 100%)

    @Column(name = "COOLING_MODE")
    private String coolingMode;     // AUTO, MANUAL, ECO

    // --- 시뮬레이션 결과 값 (PUE Page & Main) ---
    @Column(name = "CURRENT_TEMP")
    private Double currentTemp;     // 현재 실내 온도 (IT부하와 쿨링에 따라 변함)

    @Column(name = "TOTAL_IT_LOAD")
    private Long totalItLoad;       // 현재 IT 장비 총 전력량 (Watt)

    @Column(name = "TOTAL_FACILITY_LOAD")
    private Long totalFacilityLoad; // 시설 전체 전력량 (IT + 냉방 + 조명 등)

    @Column(name = "CURRENT_PUE")
    private Double currentPue;      // 실시간 PUE 값
}