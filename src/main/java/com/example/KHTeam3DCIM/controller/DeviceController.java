// 안내 데스크: DeviceController.java
// 사용자의 요청(URL)을 받아서, 만들어둔 DeviceService에게 일을 시키는 역할입니다.

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.DeviceService;
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
                       @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        List<Device> devices = deviceService.searchDevices(keyword, sort, sortDir);

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
    // @Transactional // Controller에서는 제거 (Service에서 처리)
    public String create(
            @RequestParam("rackId") Long rackId,
            @RequestParam("cateId") String cateId,
            @RequestParam(value = "reqId", required = false) Long reqId,
            Device device
    ) {
        // 1. 현재 로그인한 사용자 ID 가져오기
        String currentMemberId = SecurityContextHolder.getContext().getAuthentication().getName();
        // 2. Member 엔티티 찾아오기
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new RuntimeException("회원 정보 없음"));
        // 3. 장비에 주인 설정 (등록자 기록용)
        device.setMember(currentMember);

        // 장비 등록
        deviceService.registerDevice(rackId, cateId, device);

        // 만약 신청서 승인건이었다면, 신청서 상태를 '처리 완료'로 변경
        if (reqId != null) {
            Request req = requestRepository.findById(reqId).orElse(null);
            if (req != null) {
                req.setStatus("APPROVED");
                requestRepository.save(req); // 명시적 저장 (Transactional 없으므로)
            }
        }
        return "redirect:/devices";
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
    public String update(@PathVariable Long id, Device device) {
        deviceService.updateDevice(id, device);
        return "redirect:/devices";
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