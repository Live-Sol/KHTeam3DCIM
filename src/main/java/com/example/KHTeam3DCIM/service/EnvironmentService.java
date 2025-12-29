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


/**
 * [발표 포인트: DCIM 시뮬레이션 엔진]
 * 이 서비스는 단순한 데이터 저장을 넘어, 물리적인 데이터센터 환경을 소프트웨어로 시뮬레이션합니다.
 * IT 장비의 부하량, 팬 속도, 냉방 효율(PUE) 간의 상관관계를 수식화하여 구현했습니다.
 */
@Service
@RequiredArgsConstructor
public class EnvironmentService {

    private final DcimEnvironmentRepository envRepository;
    private final DeviceRepository deviceRepository;


    /**
     * [기술적 의도: Singleton 패턴과 유사한 데이터 관리]
     * 환경 설정 값(온도, 습도, PUE 등)은 시스템 전역에 '단 하나'만 존재해야 합니다.
     * 따라서 ID를 1L로 고정하고, 데이터가 없으면 초기값을 생성(Lazy Initialization)하는 전략을 사용했습니다.
     * 이는 불필요한 데이터 파편화를 막고 관리의 일관성을 유지합니다.
     */
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
    // 관리자 페이지에서 팬 속도나 목표 온도를 변경할 때 호출됩니다.
    @Transactional
    public void updateCoolingSettings(Double targetTemp, Integer fanSpeed, String mode) {
        DcimEnvironment env = getEnvironment();
        // Dirty Checking: JPA의 영속성 컨텍스트가 변경을 감지하여 자동으로 update 쿼리를 날립니다.
        if(targetTemp != null) env.setTargetTemp(targetTemp);
        if(fanSpeed != null) env.setFanSpeed(fanSpeed);
        if(mode != null) env.setCoolingMode(mode);

        // 설정 변경 즉시 시뮬레이션 로직을 태워, 변경된 값이 PUE나 온도에 바로 반영되도록 합니다. (반응성 향상)
        calculateSimulation(env);
    }


    /**
     * [발표 포인트: 물리 환경 시뮬레이션 알고리즘 (Core Logic)]
     * 실제 센서가 없는 개발 환경에서도 리얼한 데이터를 보여주기 위해
     * '팬 속도', 'IT 부하' 등의 변수를 기반으로 온도와 PUE를 역산출하는 로직입니다.
     */
    // ⭐ 3. 시뮬레이션 핵심 로직 (PUE & 온도 계산) ⭐
    // 이 메서드는 대시보드 새로고침 할 때나 설정 변경 시 호출됩니다.
    @Transactional
    public DcimEnvironment calculateSimulation(DcimEnvironment env) {
        if (env == null) env = getEnvironment();

        // 1. IT 장비 부하 집계 (Aggregation)
        // Java Stream API를 사용하여 리스트 순회 성능을 최적화하고 코드를 간결하게 작성했습니다.
        // 실제로는 수천 개의 장비가 있을 수 있으므로, DB단에서 SUM() 쿼리를 쓰는 것이 좋지만
        // 현재는 로직의 유연성을 위해 애플리케이션 레벨에서 처리했습니다.
        List<Device> devices = deviceRepository.findAll();
        long itLoadSum = devices.stream()
                .mapToLong(d -> d.getPowerWatt() != null ? d.getPowerWatt() : 0)
                .sum();
        if (itLoadSum == 0) itLoadSum = 1000; // Divide By Zero 방지 및 기본 부하 설정

        // 2. 쿨링 부하 계산 (Physics Algorithm)
        // "팬 속도가 빠르면(냉각 강화) -> 전력 소모가 늘어난다"는 물리 법칙을 수식화했습니다.
        // Cooling Load = (기본 IT 부하의 30%) + (팬 속도에 따른 가변 부하)
        double coolingLoad = (itLoadSum * 0.3) + (env.getFanSpeed() * 50);

        // 3. 기타 시설 부하
        double otherLoad = itLoadSum * 0.1;

        // 4. PUE(Power Usage Effectiveness) 계산
        // 데이터센터 효율 지표 공식: PUE = (Total Facility Power) / (IT Equipment Power)
        // 1.0에 가까울수록 효율적이며, 이 시스템의 핵심 KPI입니다.
        long totalLoad = (long) (itLoadSum + coolingLoad + otherLoad);
        double pue = (double) totalLoad / itLoadSum;
        pue = Math.round(pue * 100.0) / 100.0; // 소수점 절삭

        // 5. 온도 시뮬레이션 (Feedback Loop)
        // 목표 온도, 현재 부하(발열), 팬 속도(냉각)의 상관관계를 반영하여 현재 온도를 도출합니다.
        // 공식: 목표온도 + (발열 계수) - (냉각 계수) + 랜덤 노이즈(현실성 부여)
        double simulatedTemp = env.getTargetTemp() + (itLoadSum * 0.0001) - (env.getFanSpeed() * 0.05);

        // 현실적인 범위(Clamp) 설정: 온도가 비정상적으로 튀는 것을 방지
        double noise = (Math.random() - 0.5);
        simulatedTemp += noise;

        if(simulatedTemp < 18.0) simulatedTemp = 18.0;
        if(simulatedTemp > 35.0) simulatedTemp = 35.0;
        simulatedTemp = Math.round(simulatedTemp * 10.0) / 10.0;

        // 6. 결과 반영
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