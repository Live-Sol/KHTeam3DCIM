// 안내 데스크: DeviceController.java
// 사용자의 요청(URL)을 받아서, 만들어둔 DeviceService에게 일을 시키는 역할입니다.

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.DeviceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final CategoryService categoryService;
    private final RequestRepository requestRepository;
    private final RackRepository rackRepository;
    private final MemberRepository memberRepository;

    // ==========================================
    // 1. 장비 목록 페이지 보여주기 (+ 검색 기능)
    // ==========================================
    @GetMapping("/devices")
    public String list(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false, defaultValue = "latest") String sort,
                       @RequestParam(required = false, defaultValue = "asc") String sortDir,
                       HttpServletRequest request) {

        List<Device> devices = deviceService.searchDevices(keyword, sort, sortDir);
        model.addAttribute("request", request);
        model.addAttribute("devices", devices);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("sortDir", sortDir);

        return "device/device_list";
    }

    // ==========================================
    // 2. 장비 등록 화면 (수정됨: 랙ID, 위치 정보 받기)
    // ==========================================
    @GetMapping("/devices/new")
    public String createForm(Model model,
                             @RequestParam(required = false) Long reqId,       // 신청서 승인 건에서 옴
                             @RequestParam(required = false) Long rackId,      // 랙 실장도에서 옴
                             @RequestParam(required = false) Integer startUnit // 랙 실장도에서 옴
    ) {

        Device device = new Device();

        // (1) 실장도에서 왔을 때 위치 세팅
        if (rackId != null && startUnit != null) {
            device.setStartUnit(startUnit);
        }

        // (2) 신청서 승인 건 처리 (데이터 복사)
        if (reqId != null) {
            Request req = requestRepository.findById(reqId).orElse(null);
            if (req != null) {
                // 장비 스펙 복사
                device.setVendor(req.getVendor());
                device.setModelName(req.getModelName());
                device.setHeightUnit(req.getHeightUnit());

                // 계약 날짜 정보 복사
                device.setContractDate(req.getStartDate());
                device.setContractMonth(req.getTermMonth());

                // 🚑 [수술 완료] 누락되거나 잘못 연결된 정보들 수정!
                // Request의 정보를 Device에 정확히 매핑합니다.
                device.setCompanyName(req.getCompanyName());   // 회사명
                device.setCompanyPhone(req.getCompanyPhone()); // 회사 대표 번호
                device.setUserName(req.getUserName());         // 담당자 이름
                device.setContact(req.getContact());           // 담당자 연락처
                device.setDescription(req.getPurpose());       // 입고 목적 -> 설명
                device.setPowerWatt(req.getPowerWatt());       // 예상 소비 전력
                device.setEmsStatus(req.getEmsStatus());       // EMS 사용 신청

                model.addAttribute("selectedCateId", req.getCateId());
            }
        }

        // "대기 중인 신청서 목록" 가져오기 (드롭다운용)
        model.addAttribute("waitingRequests", requestRepository.findByStatusOrderByReqDateDesc("WAITING"));

        // 드롭다운용 데이터 가져오기
        List<Rack> racks = rackRepository.findAll();
        model.addAttribute("racks", racks);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("device", device);
        model.addAttribute("reqId", reqId);

        // 선택된 랙 ID 전달 (자동 선택용)
        model.addAttribute("selectedRackId", rackId);

        return "device/device_form";
    }

    // ==========================================
    // 3. 실제 등록 처리하기 (저장 버튼 눌렀을 때)
    // ==========================================
    @PostMapping("/devices/new")
    public String create(
            // ⭐ required = false를 붙여야 try 안의 if문이 작동합니다.
            @RequestParam(value = "rackId", required = false) Long rackId,
            @RequestParam(value = "cateId", required = false) String cateId,
            @RequestParam(value = "reqId", required = false) Long reqId,
            Device device,
            Model model
    ) {
        try {
            // 0. 랙 및 카테고리 선택 여부 체크 (최상단)
            if (rackId == null) {
                throw new IllegalArgumentException("설치할 랙(Rack)을 선택해야 합니다.");
            }
            if (cateId == null || cateId.trim().isEmpty()) {
                throw new IllegalArgumentException("장비 종류(Category)를 선택해야 합니다.");
            }
            // 0-1. 필수 입력값 빈 값 체크
            if (device.getCompanyName() == null || device.getCompanyName().trim().isEmpty()) {
                throw new IllegalArgumentException("회사명은 필수 입력 항목입니다.");
            }
            if (device.getUserName() == null || device.getUserName().trim().isEmpty()) {
                throw new IllegalArgumentException("담당자 이름은 필수 입력 항목입니다.");
            }
            if (device.getContact() == null || device.getContact().trim().isEmpty()) {
                throw new IllegalArgumentException("담당자 연락처는 필수 입력 항목입니다.");
            }
            if (device.getSerialNum() == null || device.getSerialNum().trim().isEmpty()) {
                throw new IllegalArgumentException("시리얼 번호는 필수 입력 항목입니다.");
            }
            if (device.getStartUnit() == null || device.getStartUnit() < 1) {
                throw new IllegalArgumentException("올바른 시작 유닛 번호를 입력해주세요.");
            }
            if (device.getHeightUnit() == null || device.getHeightUnit() < 1) {
                throw new IllegalArgumentException("장비 높이는 최소 1U 이상이어야 합니다.");
            }

            // 1. 시리얼 번호 중복 체크
            if (deviceService.isSerialDuplicate(device.getSerialNum(), null)) {
                throw new IllegalStateException("이미 등록된 시리얼 번호입니다.");
            }

            // 2. 랙 공간 점유 체크
            deviceService.checkRackOverlap(rackId, device.getStartUnit(), device.getHeightUnit(), null);

            // 3. 정상 로직 진행
            String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
            Member currentMember = memberRepository.findById(currentMemberId)
                    .orElseThrow(() -> new RuntimeException("회원 정보 없음"));
            device.setMember(currentMember);

            // 4. 서비스 호출 및 저장
            deviceService.registerDevice(rackId, cateId, device);

            // 5. 신청서 상태 업데이트
            if (reqId != null) {
                requestRepository.findById(reqId).ifPresent(req -> {
                    req.setStatus("APPROVED");
                    requestRepository.save(req);
                });
            }

            return "redirect:/devices";

        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());

            // 폼 데이터 및 상태 유지
            model.addAttribute("racks", rackRepository.findAll());
            model.addAttribute("categories", categoryService.findAllCategories());
            model.addAttribute("device", device);
            model.addAttribute("selectedRackId", rackId);
            model.addAttribute("selectedCateId", cateId);
            model.addAttribute("reqId", reqId);

            return "device/device_form";
        }
    }

    // ==========================================
    // 4. 장비 삭제
    // ==========================================
    @GetMapping("/devices/{id}/delete")
    public String delete(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return "redirect:/devices";
    }

    // ==========================================
    // 5. 수정 화면 보여주기
    // ==========================================
    @GetMapping("/devices/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Device device = deviceService.findById(id);

        model.addAttribute("racks", rackRepository.findAll());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("device", device);
        model.addAttribute("isEdit", true);

        return "device/device_form";
    }

    // ==========================================
    // 6. 실제 수정 처리
    // ==========================================
    @PostMapping("/devices/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam(value = "rackId", required = false) Long rackId, // HTML의 name="rackId"와 매핑
                         @RequestParam(value="cateId", required=false) String cateId,
                         Device device,
                         Model model) {
        try {
            // 0. 랙 선택 여부 체크를 최상단에 배치합니다.
            if (rackId == null) {
                throw new IllegalArgumentException("설치할 랙(Rack)을 선택해야 합니다.");
            }
            // 0-1. 필수 입력값 빈 값 체크 (서버 측 검증)
            if (device.getSerialNum() == null || device.getSerialNum().trim().isEmpty()) {
                throw new IllegalArgumentException("시리얼 번호는 필수 입력 항목입니다.");
            }
            if (device.getStartUnit() == null || device.getStartUnit() < 1) {
                throw new IllegalArgumentException("올바른 시작 유닛 번호를 입력해주세요.");
            }
            if (device.getHeightUnit() == null || device.getHeightUnit() < 1) {
                throw new IllegalArgumentException("장비 높이는 최소 1U 이상이어야 합니다.");
            }

            // 1. 시리얼 번호 중복 체크
            if (deviceService.isSerialDuplicate(device.getSerialNum(), id)) {
                throw new IllegalStateException("이미 다른 장비에서 사용 중인 시리얼 번호입니다.");
            }

            // 2. 랙 공간 점유 체크 (조건문 단순화: rackId는 필수값이므로 바로 체크)
            deviceService.checkRackOverlap(rackId, device.getStartUnit(), device.getHeightUnit(), id);

            // 3. 모든 검증 통과 시 실제 수정 로직 수행
            deviceService.updateDevice(id, device, rackId, cateId);

            return "redirect:/devices";

        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("racks", rackRepository.findAll());
            model.addAttribute("categories", categoryService.findAllCategories());
            model.addAttribute("device", device);
            model.addAttribute("isEdit", true);

            // 선택 값 유지 (rackId 사용)
            model.addAttribute("selectedRackId", rackId);

            // 카테고리는 select name="category.id"일 경우 아래와 같이 처리
            if (device.getCategory() != null) {
                model.addAttribute("selectedCateId", device.getCategory().getId());
            }

            return "device/device_form";
        }
    }

    // ==========================================
    // 7. 모달 팝업용 JSON 데이터 반환 API
    // ==========================================
    @GetMapping("/api/devices/{id}")
    @ResponseBody
    public Device getDeviceDetailApi(@PathVariable Long id) {
        return deviceService.findById(id);
    }

    // ==========================================
    // 8. 전원 변경 API (AJAX용)
    // ==========================================
    @PostMapping("/api/devices/{id}/toggle-status")
    @ResponseBody
    public String toggleDeviceStatus(@PathVariable Long id) {
        return deviceService.toggleStatus(id);
    }
}