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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @RequestParam(value = "rackId", required = false) Long rackId,
            @RequestParam(value = "cateId", required = false) String cateId,
            @RequestParam(value = "reqId", required = false) Long reqId,
            Device device,
            RedirectAttributes rttr  // 1. Model 대신 RedirectAttributes 추가
    ) {
        try {
            // [순서 1] 필수값 검증 로직 (기존과 동일)
            if (device.getCompanyName() == null || device.getCompanyName().trim().isEmpty()) throw new IllegalArgumentException("회사명은 필수 입력 항목입니다.");
            if (device.getCompanyPhone() == null || device.getCompanyPhone().trim().isEmpty()) throw new IllegalArgumentException("회사 대표 번호는 필수 입력 항목입니다.");
            if (device.getUserName() == null || device.getUserName().trim().isEmpty()) throw new IllegalArgumentException("담당자 이름은 필수 입력 항목입니다.");
            if (device.getContact() == null || device.getContact().trim().isEmpty()) throw new IllegalArgumentException("담당자 연락처는 필수 입력 항목입니다.");

            // 공통 검증 및 날짜 동기화
            validateAndSync(device);

            if (rackId == null) throw new IllegalArgumentException("설치할 랙(Rack)을 선택해야 합니다.");
            if (cateId == null || cateId.trim().isEmpty()) throw new IllegalArgumentException("장비 종류(Category)를 선택해야 합니다.");
            if (device.getSerialNum() == null || device.getSerialNum().trim().isEmpty()) throw new IllegalArgumentException("시리얼 번호는 필수 입력 항목입니다.");

            // [순서 4] 비즈니스 로직 검증
            if (deviceService.isSerialDuplicate(device.getSerialNum(), null)) throw new IllegalStateException("이미 등록된 시리얼 번호입니다.");
            deviceService.checkRackOverlap(rackId, device.getStartUnit(), device.getHeightUnit(), null);

            // 저장 로직 (기존과 동일)
            String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
            Member currentMember = memberRepository.findById(currentMemberId).orElseThrow(() -> new RuntimeException("회원 정보 없음"));
            device.setMember(currentMember);
            deviceService.registerDevice(rackId, cateId, device);

            if (reqId != null) {
                requestRepository.findById(reqId).ifPresent(req -> {
                    req.setStatus("APPROVED");
                    requestRepository.save(req);
                });
            }

            rttr.addFlashAttribute("successMessage", "장비가 성공적으로 등록되었습니다!");
            return "redirect:/devices";

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 2. 핵심 변경 부분: FlashAttribute에 에러 메시지 저장
            rttr.addFlashAttribute("errorMessage", e.getMessage());

            // 3. 사용자가 입력하던 데이터도 가방에 담아서 보냄 (입력폼 유지용)
            rttr.addFlashAttribute("device", device);

            // 4. URL 파라미터 전달 (신청서 ID나 선택된 값들 유지)
            if (reqId != null) rttr.addAttribute("reqId", reqId);
            if (rackId != null) rttr.addAttribute("rackId", rackId);

            // 5. 다시 등록 폼 페이지로 "리다이렉트"
            return "redirect:/devices/new";
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
                         @RequestParam(value = "rackId", required = false) Long rackId,
                         @RequestParam(value="cateId", required=false) String cateId,
                         Device device,
                         RedirectAttributes rttr) { // 1. Model 대신 RedirectAttributes 추가
        try {
            // [검증 로직] - 기존과 동일
            if (device.getCompanyName() == null || device.getCompanyName().trim().isEmpty()) throw new IllegalArgumentException("회사명은 필수 입력 항목입니다.");
            if (device.getCompanyPhone() == null || device.getCompanyPhone().trim().isEmpty()) throw new IllegalArgumentException("회사 대표 번호는 필수 입력 항목입니다.");
            if (device.getUserName() == null || device.getUserName().trim().isEmpty()) throw new IllegalArgumentException("담당자 이름은 필수 입력 항목입니다.");
            if (device.getContact() == null || device.getContact().trim().isEmpty()) throw new IllegalArgumentException("담당자 연락처는 필수 입력 항목입니다.");

            // 공통 검증 및 날짜 동기화 호출
            validateAndSync(device);

            if (rackId == null) throw new IllegalArgumentException("설치할 랙(Rack)을 선택해야 합니다.");
            if (cateId == null || cateId.trim().isEmpty()) throw new IllegalArgumentException("장비 종류(Category)를 선택해야 합니다.");
            if (device.getSerialNum() == null || device.getSerialNum().trim().isEmpty()) throw new IllegalArgumentException("시리얼 번호는 필수 입력 항목입니다.");
            if (device.getStartUnit() == null || device.getStartUnit() < 1) throw new IllegalArgumentException("올바른 시작 유닛 번호를 입력해주세요.");
            if (device.getHeightUnit() == null || device.getHeightUnit() < 1) throw new IllegalArgumentException("장비 높이는 최소 1U 이상이어야 합니다.");

            if (deviceService.isSerialDuplicate(device.getSerialNum(), id)) {
                throw new IllegalStateException("이미 다른 장비에서 사용 중인 시리얼 번호입니다.");
            }
            deviceService.checkRackOverlap(rackId, device.getStartUnit(), device.getHeightUnit(), id);

            // 모든 검증 통과 시 저장
            deviceService.updateDevice(id, device, rackId, cateId);

            // 성공 메시지 (선택 사항)
            rttr.addFlashAttribute("successMessage", "장비 정보가 수정되었습니다.");
            return "redirect:/devices";

        } catch (IllegalStateException | IllegalArgumentException e) {
            // 2. 에러 메시지를 FlashAttribute에 담기 (토스트용)
            rttr.addFlashAttribute("errorMessage", e.getMessage());

            // 3. 입력했던 데이터 가방에 담기 (기존 입력값 유지용)
            // 화면의 th:value="${device.companyName}" 등이 이 가방에서 데이터를 꺼내 쓰게 됩니다.
            rttr.addFlashAttribute("device", device);

            // 4. 리다이렉트 시 필요한 정보들을 가방에 담기
            rttr.addFlashAttribute("isEdit", true);

            // 5. 다시 '수정 화면'으로 리다이렉트 (경로에 id 포함)
            return "redirect:/devices/" + id + "/edit";
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

    // -----------------------------------------------------------
    // ⭐ [여기 아래에 추가] 공통 검증 및 데이터 동기화 로직
    // -----------------------------------------------------------
    private void validateAndSync(Device device) {
        // 1. 휴대폰/연락처 정규식 검사
        String phoneRegex = "^\\d{2,3}-\\d{3,4}-\\d{4}$";

        if (device.getCompanyPhone() != null && !device.getCompanyPhone().matches(phoneRegex)) {
            throw new IllegalArgumentException("회사 대표 번호 형식이 올바르지 않습니다. (예: 02-123-4567)");
        }
        if (device.getContact() != null && !device.getContact().matches(phoneRegex)) {
            throw new IllegalArgumentException("담당자 연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)");
        }

        // 2. 날짜 데이터 동기화 (ContractDate -> RegDate)
        if (device.getContractDate() != null) {
            device.setRegDate(device.getContractDate().atStartOfDay());
        }
    }

}