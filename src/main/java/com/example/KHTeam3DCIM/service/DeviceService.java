// DeviceService.java (핵심 두뇌)
// 이 파일에는 3가지 핵심 로직이 들어갑니다.
// 1.조회: 장비 목록 가져오기.
// 2.검사: "잠깐! 10번 칸에 이미 서버가 있는데?" (충돌 체크 로직 ⭐)
// 3.저장 & 기록: 장비를 저장하고, 동시에 로그(DcLog)에도 "누가 등록함"이라고 기록 남기기.

package com.example.KHTeam3DCIM.service;

import com.example.KHTeam3DCIM.domain.Category;
import com.example.KHTeam3DCIM.domain.DcLog;
import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto; // [추가] DTO 위치 확인!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.KHTeam3DCIM.repository.CategoryRepository;
import com.example.KHTeam3DCIM.repository.DcLogRepository;
import com.example.KHTeam3DCIM.repository.DeviceRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor        // final 붙은 친구들을 자동으로 생성자 주입 (Autowired 대체)
@Transactional(readOnly = true) // 기본적으로는 '읽기 전용' (성능 최적화)
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RackRepository rackRepository;
    private final CategoryRepository categoryRepository;
    private final DcLogRepository dcLogRepository;

    // ==========================================
    // 1. 장비 등록하기 (핵심 기능)
    // ==========================================
    @Transactional // 이건 '쓰기' 작업이니까 Transactional 필수!
    public Long registerDevice(Long rackId, String cateId, Device newDevice) {

        // (1) 랙이 진짜 있는지 확인 (빈자리 체크)
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 랙입니다."));

        // (2) 카테고리가 진짜 있는지 확인
        Category category = categoryRepository.findById(cateId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // ⭐ (3) 위치 충돌 체크 (이 서비스의 존재 이유!)
        // 해당 랙에 이미 꽂혀있는 장비들을 다 가져와서 검사합니다.
        List<Device> existingDevices = deviceRepository.findByRackId(rackId);

        // 새로 꽂을 장비의 범위 (예: 10번부터 2칸이면 -> 10, 11)
        int newStart = newDevice.getStartUnit();
        int newEnd = newStart + newDevice.getHeightUnit() - 1;

        // 랙 전체 높이(42U)를 벗어나는지 체크
        if (newEnd > rack.getTotalUnit()) {
            throw new IllegalStateException("장비가 랙 높이를 벗어납니다.");
        }

        // 기존 장비들과 겹치는지 하나씩 비교
        for (Device existing : existingDevices) {
            int exStart = existing.getStartUnit();
            int exEnd = exStart + existing.getHeightUnit() - 1;

            // 겹침 공식: (A시작 <= B끝) AND (A끝 >= B시작) 이면 겹친 것임
            if (newStart <= exEnd && newEnd >= exStart) {
                throw new IllegalStateException("이미 해당 위치(" + exStart + "~" + exEnd + "U)에 장비가 있습니다.");
            }
        }

        // (4) 통과했으면 관계 맺어주기
        newDevice.setRack(rack);
        newDevice.setCategory(category);

        // (5) 장비 저장 (DB에 INSERT)
        deviceRepository.save(newDevice);

        // (6) 로그 남기기 (CCTV 녹화)
        // 실제로는 로그인한 사용자 ID를 넣어야 하지만, 지금은 'admin'으로 고정
        DcLog log = DcLog.builder()
                .memberId("admin")
                .targetDevice(newDevice.getSerialNum())
                .actionType("INSERT")
                .build();
        dcLogRepository.save(log);

        return newDevice.getId();
    }

    // ==========================================
    // 2. 조회 기능들
    // ==========================================

    // 전체 장비 목록 가져오기
    public List<Device> findAllDevices() {
        return deviceRepository.findAll();
    }
    // 특정 랙에 있는 장비만 가져오기
    public List<Device> findDevicesByRack(Long rackId) {
        return deviceRepository.findByRackId(rackId);
    }
    // 시리얼 넘버로 찾기 (검색)
    public Device findBySerial(String serialNum) {
        return deviceRepository.findBySerialNum(serialNum)
                .orElse(null);
    }
    // ID로 장비 찾기 (수정 화면용)
    public Device findById(Long id) {
        return deviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("없음"));
    }


    // =========================================================
    // ⭐ 3. 랙 실장도 데이터 가공 로직 (HTML 렌더링 순서에 맞춰 수정)
    // =========================================================
    public List<RackDetailDto> getRackViewData(Long rackId) {

        // 1. 랙 높이 확인
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new IllegalArgumentException("없는 랙입니다."));
        int totalHeight = rack.getTotalUnit().intValue();

        // 2. 빈 배열 생성
        RackDetailDto[] slots = new RackDetailDto[totalHeight + 1];
        for (int i = 1; i <= totalHeight; i++) {
            slots[i] = RackDetailDto.builder()
                    .unitNum(i)
                    .status("EMPTY")
                    .deviceName("")
                    .rowSpan(1)
                    .build();
        }

        // 3. 장비 배치
        List<Device> devices = deviceRepository.findByRackId(rackId);

        for (Device d : devices) {
            int start = d.getStartUnit();   // 예: 10번
            int height = d.getHeightUnit(); // 예: 2U
            int end = start + height - 1;   // 예: 11번 (여기가 Top!)

            // 유효성 검사 (범위 넘어가면 패스)
            if (end > totalHeight) continue;

            // (1) ⭐ 핵심 수정: 가장 위쪽 칸(Top)에 정보를 넣고 rowspan을 건다!
            // HTML은 위->아래로 그려지므로, 위쪽 칸이 '주인'이 되어야 아래를 덮습니다.
            slots[end].setStatus("FULL");
            slots[end].setDeviceName(d.getVendor() + " " + d.getModelName());

            if (d.getCategory() != null) {
                slots[end].setType(d.getCategory().getId());
            } else {
                slots[end].setType("ETC");
            }
            slots[end].setRowSpan(height);
            slots[end].setDeviceId(d.getId());
            slots[end].setRunStatus(d.getStatus()); // 장비 상태(RUNNING/OFF)

            // (2) 나머지 아래쪽 칸들은 'SKIP' (Top 칸이 덮어주므로 비워둠)
            for (int j = start; j < end; j++) {
                slots[j].setStatus("SKIP");
                slots[j].setRunStatus(d.getStatus()); // 아래쪽 칸들도 "형님(맨 윗칸)"을 따라서 상태 정보를 가짐
            }
        }

        // 4. 리스트로 변환 (42층 -> 1층 순서)
        List<RackDetailDto> result = new ArrayList<>();
        for (int i = totalHeight; i >= 1; i--) {
            result.add(slots[i]);
        }

        return result;
    }


    // ==========================================
    // 4. 장비 검색 기능
    // ==========================================
    public List<Device> searchDevices(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllDevices(); // 검색어 없으면 전체 조회
        }
        // 제조사, 모델명, 시리얼 3군데서 다 찾아봄
        return deviceRepository.findByVendorContainingIgnoreCaseOrModelNameContainingIgnoreCaseOrSerialNumContainingIgnoreCase(keyword, keyword, keyword);
    }

    // ==========================================
    // 5. 장비 삭제 기능
    // ==========================================
    @Transactional
    public void deleteDevice(Long id) {
        // 1. 장비가 있는지 확인
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("없는 장비입니다."));

        // 2. 로그 남기기 (삭제 이력)
        DcLog log = DcLog.builder()
                .memberId("admin") // 나중엔 실제 로그인한 ID
                .targetDevice(device.getSerialNum())
                .actionType("DELETE")
                .build();
        dcLogRepository.save(log);

        // 3. 진짜 삭제
        deviceRepository.delete(device);
    }

    // ==========================================
    // 6. 장비 정보 수정 (Dirty Checking)
    // ==========================================
    @Transactional
    public void updateDevice(Long id, Device formDevice) {
        // 1. 기존 데이터 조회
        Device target = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));

        // 2. 정보 수정 (덮어쓰기)
        target.setVendor(formDevice.getVendor());
        target.setModelName(formDevice.getModelName());
        target.setSerialNum(formDevice.getSerialNum());
        target.setIpAddr(formDevice.getIpAddr());
        // (필요하다면 카테고리 등 다른 정보도 여기서 수정)
        // target.setCategory(categoryRepository.findById(formDevice.getCategory().getId())...);

        // 3. ⭐ 로그 남기기 (이 부분이 있어야 대시보드에 뜹니다!) ⭐
        DcLog log = DcLog.builder()
                .memberId("admin")
                .targetDevice(target.getSerialNum())
                .actionType("UPDATE") // "UPDATE"라고 적어야 초록색 '수정' 글씨가 뜸
                .build();

        dcLogRepository.save(log);

        // 4. save() 호출 안 해도 트랜잭션이 끝나면 알아서 DB에 반영됨 (Dirty Checking)
    }

    // ==========================================
    // 7. 전원 스위치 (ON <-> OFF)
    // ==========================================
    @Transactional
    public String toggleStatus(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장비가 없습니다."));

        // 현재 상태가 RUNNING이면 OFF로, 아니면 RUNNING으로 변경
        if ("RUNNING".equals(device.getStatus())) {
            device.setStatus("OFF");
        } else {
            device.setStatus("RUNNING");
        }

        // 로그 남기기
        dcLogRepository.save(DcLog.builder()
                .memberId("admin")
                .targetDevice(device.getSerialNum())
                .actionType("POWER_" + device.getStatus())
                .build());


        return device.getStatus(); // 변경된 상태 리턴
    }

}
