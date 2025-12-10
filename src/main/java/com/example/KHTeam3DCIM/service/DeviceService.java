//// DeviceService.java (핵심 두뇌)
//// 이 파일에는 3가지 핵심 로직이 들어갑니다.
//// 1.조회: 장비 목록 가져오기.
//// 2.검사: "잠깐! 10번 칸에 이미 서버가 있는데?" (충돌 체크 로직 ⭐)
//// 3.저장 & 기록: 장비를 저장하고, 동시에 로그(DcLog)에도 "누가 등록함"이라고 기록 남기기.
//
//package com.example.KHTeam3DCIM.service;
//
//import com.example.KHTeam3DCIM.domain.Category;
//import com.example.KHTeam3DCIM.domain.DcLog;
//import com.example.KHTeam3DCIM.domain.Device;
//import com.example.KHTeam3DCIM.domain.Rack;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import com.example.KHTeam3DCIM.repository.CategoryRepository;
//import com.example.KHTeam3DCIM.repository.DcLogRepository;
//import com.example.KHTeam3DCIM.repository.DeviceRepository;
//import com.example.KHTeam3DCIM.repository.RackRepository;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor        // final 붙은 친구들을 자동으로 생성자 주입 (Autowired 대체)
//@Transactional(readOnly = true) // 기본적으로는 '읽기 전용' (성능 최적화)
//public class DeviceService {
//
//    private final DeviceRepository deviceRepository;
//    private final RackRepository rackRepository;
//    private final CategoryRepository categoryRepository;
//    private final DcLogRepository dcLogRepository;
//
//    // ==========================================
//    // 1. 장비 등록하기 (핵심 기능)
//    // ==========================================
//    @Transactional // 이건 '쓰기' 작업이니까 Transactional 필수!
//    public Long registerDevice(Long rackId, String cateId, Device newDevice) {
//
//        // (1) 랙이 진짜 있는지 확인 (빈자리 체크)
//        Rack rack = rackRepository.findById(rackId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 랙입니다."));
//
//        // (2) 카테고리가 진짜 있는지 확인
//        Category category = categoryRepository.findById(cateId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
//
//        // ⭐ (3) 위치 충돌 체크 (이 서비스의 존재 이유!)
//        // 해당 랙에 이미 꽂혀있는 장비들을 다 가져와서 검사합니다.
//        List<Device> existingDevices = deviceRepository.findByRackId(rackId);
//
//        // 새로 꽂을 장비의 범위 (예: 10번부터 2칸이면 -> 10, 11)
//        int newStart = newDevice.getStartUnit();
//        int newEnd = newStart + newDevice.getHeightUnit() - 1;
//
//        // 랙 전체 높이(42U)를 벗어나는지 체크
//        if (newEnd > rack.getTotalUnit()) {
//            throw new IllegalStateException("장비가 랙 높이를 벗어납니다.");
//        }
//
//        // 기존 장비들과 겹치는지 하나씩 비교
//        for (Device existing : existingDevices) {
//            int exStart = existing.getStartUnit();
//            int exEnd = exStart + existing.getHeightUnit() - 1;
//
//            // 겹침 공식: (A시작 <= B끝) AND (A끝 >= B시작) 이면 겹친 것임
//            if (newStart <= exEnd && newEnd >= exStart) {
//                throw new IllegalStateException("이미 해당 위치(" + exStart + "~" + exEnd + "U)에 장비가 있습니다.");
//            }
//        }
//
//        // (4) 통과했으면 관계 맺어주기
//        newDevice.setRack(rack);
//        newDevice.setCategory(category);
//
//        // (5) 장비 저장 (DB에 INSERT)
//        deviceRepository.save(newDevice);
//
//        // (6) 로그 남기기 (CCTV 녹화)
//        // 실제로는 로그인한 사용자 ID를 넣어야 하지만, 지금은 'admin'으로 고정
//        DcLog log = DcLog.builder()
//                .memberId("admin")
//                .targetDevice(newDevice.getSerialNum())
//                .actionType("INSERT")
//                .build();
//        dcLogRepository.save(log);
//
//        return newDevice.getId();
//    }
//
//    // ==========================================
//    // 2. 조회 기능들
//    // ==========================================
//
//    // 전체 장비 목록 가져오기
//    public List<Device> findAllDevices() {
//        return deviceRepository.findAll();
//    }
//
//    // 특정 랙에 있는 장비만 가져오기
//    public List<Device> findDevicesByRack(Long rackId) {
//        return deviceRepository.findByRackId(rackId);
//    }
//
//    // 시리얼 넘버로 찾기 (검색)
//    public Device findBySerial(String serialNum) {
//        return deviceRepository.findBySerialNum(serialNum)
//                .orElse(null);
//    }
//}
