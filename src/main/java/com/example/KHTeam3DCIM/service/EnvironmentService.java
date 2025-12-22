package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.DcimEnvironment;
import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.DcimEnvironmentRepository; // (Repository도 생성 필요)
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final DcimEnvironmentRepository envRepository;
    private final DeviceRepository deviceRepository;

    // 1. 현재 환경 상태 가져오기 (없으면 기본값 생성)
    @Transactional
    public DcimEnvironment getEnvironment() {
        return envRepository.findById(1L).orElseGet(() -> {
            DcimEnvironment initEnv = DcimEnvironment.builder()
                    .id(1L)
                    .targetTemp(24.0)      // 목표 24도
                    .currentTemp(25.5)     // 현재 25.5도
                    .fanSpeed(50)          // 팬 속도 50%
                    .coolingMode("AUTO")   // 자동 모드
                    .totalItLoad(0L)       // 초기 IT 부하 0W
                    .totalFacilityLoad(0L) // 초기 시설 부하 0W
                    .currentPue(1.5)       // 초기 PUE 1.5
                    .build();              // 빌더 패턴으로 객체 생성
            return envRepository.save(initEnv);
        });
    }

    // 2. 쿨링 설정 조절 (관리자용)
    @Transactional
    public void updateCoolingSettings(Double targetTemp, Integer fanSpeed, String mode) {
        DcimEnvironment env = getEnvironment();
        if(targetTemp != null) env.setTargetTemp(targetTemp);
        if(fanSpeed != null) env.setFanSpeed(fanSpeed);
        if(mode != null) env.setCoolingMode(mode);

        // 설정 변경 시 즉시 시뮬레이션 한 번 돌리기
        calculateSimulation(env);
    }

    // ⭐ 3. 시뮬레이션 핵심 로직 (PUE & 온도 계산) ⭐
    // 이 메서드는 대시보드 새로고침 할 때나 설정 변경 시 호출됩니다.
    @Transactional
    public DcimEnvironment calculateSimulation(DcimEnvironment env) {
        if (env == null) env = getEnvironment();

        // (1) IT 장비 총 부하 계산 (DB에 있는 모든 RUNNING 장비의 Watt 합계)
        // 편의상 모든 장비의 합으로 하거나, 추후 status='RUNNING' 조건 추가 가능
        List<Device> devices = deviceRepository.findAll();
        long itLoadSum = devices.stream()
                .mapToLong(d -> d.getPowerWatt() != null ? d.getPowerWatt() : 0) // null이면 0 처리
                .sum();

        // 장비가 하나도 없으면 기본값 설정 (0으로 나누기 방지)
        if (itLoadSum == 0) itLoadSum = 1000; // 기본 1kW 가정

        // (2) 쿨링 부하 시뮬레이션 (상상력 발휘!)
        // 공식: 팬 속도가 빠를수록 전기를 많이 먹음. 온도를 낮추려면 전기를 많이 먹음.
        // Cooling_Watt = (기본운영전력) + (팬속도 가중치)
        double coolingLoad = (itLoadSum * 0.3) + (env.getFanSpeed() * 50);

        // (3) 기타 시설 부하 (조명, 보안 등 - IT부하의 10% 가정)
        double otherLoad = itLoadSum * 0.1;

        // (4) 총 시설 전력 = IT + Cooling + Other
        long totalLoad = (long) (itLoadSum + coolingLoad + otherLoad);

        // (5) PUE 계산 = Total / IT
        double pue = (double) totalLoad / itLoadSum;
        // 소수점 둘째 자리까지 자르기
        pue = Math.round(pue * 100.0) / 100.0;

        // (6) 실내 온도 시뮬레이션
        // IT부하가 높으면 온도가 오르고, 팬속도가 높으면 온도가 내려감
        // 현재온도 = 목표온도 + (IT부하 계수) - (팬속도 계수)
        double simulatedTemp = env.getTargetTemp() + (itLoadSum * 0.0001) - (env.getFanSpeed() * 0.05);
        // -0.5 ~ +0.5 사이의 랜덤 노이즈 추가 (센서 오차/환경 변수 시뮬레이션)
        double noise = (Math.random() - 0.5);
        simulatedTemp += noise;
        // 현실적인 범위 제한 (18도 ~ 35도)
        if(simulatedTemp < 18.0) simulatedTemp = 18.0;
        if(simulatedTemp > 35.0) simulatedTemp = 35.0;
        simulatedTemp = Math.round(simulatedTemp * 10.0) / 10.0;

        // (7) 결과 저장
        env.setTotalItLoad(itLoadSum);
        env.setTotalFacilityLoad(totalLoad);
        env.setCurrentPue(pue);
        env.setCurrentTemp(simulatedTemp);

        return envRepository.save(env);
    }

    // 애플리케이션 시작 시 기본 환경 설정 데이터가 없으면 생성 (서버 켜질 때 딱 1번 실행되어 설정값 존재 여부 체크)
    @PostConstruct
    public void init() {
        if (!envRepository.existsById(1L)) {
            getEnvironment(); // 없으면 생성 로직 수행됨
            System.out.println("✅ DCIM 기본 환경 설정 데이터가 초기화되었습니다.");
        }
    }
}