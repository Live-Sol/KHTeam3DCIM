// 안내 데스크: DeviceController.java
// 사용자의 요청(URL)을 받아서, 만들어둔 DeviceService에게 일을 시키는 역할입니다.

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.*;
import com.example.KHTeam3DCIM.dto.device.deviceDTO;
import com.example.KHTeam3DCIM.repository.MemberRepository;
import com.example.KHTeam3DCIM.repository.RackRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.DeviceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

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
                             @RequestParam(required = false) Long reqId,
                             @RequestParam(required = false) Long rackId,
                             @RequestParam(required = false) Integer startUnit,
                             @RequestParam(required = false) String cateId
    ) {
        // 1️⃣ 에러 리다이렉트가 아닌 경우만 새 Device 생성
        if (!model.containsAttribute("device")) {
            Device device = new Device();

            // (1) 실장도에서 왔을 때 위치 세팅
            if (rackId != null && startUnit != null) {
                device.setStartUnit(startUnit);
            }

            // (2) 신청서 승인 건 → 값 복사
            if (reqId != null) {
                Request req = requestRepository.findById(reqId).orElse(null);
                if (req != null) {
                    device.setVendor(req.getVendor());
                    device.setModelName(req.getModelName());
                    device.setHeightUnit(req.getHeightUnit());
                    device.setContractDate(req.getStartDate());
                    device.setContractMonth(req.getTermMonth());
                    device.setCompanyName(req.getCompanyName());
                    device.setCompanyPhone(req.getCompanyPhone());
                    device.setUserName(req.getUserName());
                    device.setContact(req.getContact());
                    device.setDescription(req.getPurpose());
                    device.setPowerWatt(req.getPowerWatt());
                    device.setEmsStatus(req.getEmsStatus());
                }
            }

            model.addAttribute("device", device);
        }

        // 2️⃣ 카테고리 선택값 결정 (⭐ 핵심)
        String resolvedCateId = null;

        if (reqId != null) {
            Request req = requestRepository.findById(reqId).orElse(null);
            if (req != null) {
                resolvedCateId = req.getCateId(); // 신청서 값 우선
            }
        }

        if (resolvedCateId == null) {
            resolvedCateId = cateId; // 에러 리다이렉트 시 유지
        }

        // 3️⃣ 공통 모델 데이터
        model.addAttribute("selectedCateId", resolvedCateId);
        model.addAttribute("selectedRackId", rackId);
        model.addAttribute("waitingRequests",
                requestRepository.findByStatusOrderByReqDateDesc("WAITING"));
        model.addAttribute("racks", rackRepository.findAll());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("reqId", reqId);

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
            @ModelAttribute Device device,
            RedirectAttributes rttr  // 1. Model 대신 RedirectAttributes 추가
    ) {
        try {
            // [순서 1] 필수값 검증 로직 (기존과 동일)
            if (device.getCompanyName() == null || device.getCompanyName().trim().isEmpty()) throw new IllegalArgumentException("회사명은 필수 입력 항목입니다.");
            if (device.getCompanyPhone() == null || device.getCompanyPhone().trim().isEmpty()) throw new IllegalArgumentException("회사 대표 번호는 필수 입력 항목입니다.");
            String phoneRegex = "^\\d{2,3}-\\d{3,4}-\\d{4}$";

            if (device.getCompanyPhone() != null && !device.getCompanyPhone().matches(phoneRegex)) {
                throw new IllegalArgumentException("회사 대표 번호 형식이 올바르지 않습니다. (예: 02-123-4567)");
            }
            if (device.getUserName() == null || device.getUserName().trim().isEmpty()) throw new IllegalArgumentException("담당자 이름은 필수 입력 항목입니다.");
            if (device.getContact() == null || device.getContact().trim().isEmpty()) throw new IllegalArgumentException("담당자 연락처는 필수 입력 항목입니다.");
            if (device.getContact() != null && !device.getContact().matches(phoneRegex)) {
                throw new IllegalArgumentException("담당자 연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)");
            }

            if (rackId == null) throw new IllegalArgumentException("설치할 랙(Rack)을 선택해야 합니다.");
            // 제조사/모델명 체크
            if (device.getVendor() == null || device.getVendor().isBlank()) {
                throw new IllegalArgumentException("제조사를 입력해주세요.");
            }
            if (device.getModelName() == null || device.getModelName().isBlank()) {
                throw new IllegalArgumentException("모델명을 입력해주세요.");
            }
            if (cateId == null || cateId.trim().isEmpty()) throw new IllegalArgumentException("장비 종류(Category)를 선택해야 합니다.");
            if (device.getSerialNum() == null || device.getSerialNum().trim().isEmpty()) throw new IllegalArgumentException("시리얼 번호는 필수 입력 항목입니다.");

            // 시작유닛/높이 체크 (null 체크 포함하여 400 에러 방지)
            if (device.getStartUnit() == null || device.getStartUnit() < 1) {
                throw new IllegalArgumentException("올바른 시작 유닛을 입력해주세요. (최소 1U)");
            }
            if (device.getHeightUnit() == null || device.getHeightUnit() < 1) {
                throw new IllegalArgumentException("올바른 장비 높이를 입력해주세요. (최소 1U)");
            }
            // 1. 필수 값 체크 (비어있으면 에러)
            if (device.getIpAddr() == null || device.getIpAddr().isBlank()) {
                throw new IllegalArgumentException("관리 IP는 필수 입력 항목입니다.");
            }

            // 2. 형식 체크 (필수 값이 채워졌으니 정규식 검사)
            String ipRegex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
            if (!device.getIpAddr().matches(ipRegex)) {
                throw new IllegalArgumentException("관리 IP 형식이 올바르지 않습니다. (예: 192.168.0.1)");
            }

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
            if (rackId != null) rttr.addAttribute("rackId", rackId);
            if (reqId != null) rttr.addAttribute("reqId", reqId);
            if (cateId != null) rttr.addAttribute("cateId", cateId); // 카테고리 ID 파라미터 유지

            // 5. 다시 등록 폼 페이지로 "리다이렉트"
            return "redirect:/devices/new";
        }
    }

    // ==========================================
    // 4. 장비 삭제
    // ==========================================
    @GetMapping("/devices/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes rttr) {
        try {
            deviceService.deleteDevice(id);
            rttr.addFlashAttribute("successMessage", "장비가 목록에서 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            // 삭제 실패 시 에러 메시지 전달
            rttr.addFlashAttribute("errorMessage", "장비 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/devices";
    }

    // ==========================================
    // 5. 수정 화면 보여주기
    // ==========================================
    @GetMapping("/devices/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {

        // 🔹 1. device 우선순위 처리 (에러 리다이렉트 대응)
        Device device;
        if (model.containsAttribute("device")) {
            device = (Device) model.getAttribute("device");
        } else {
            device = deviceService.findById(id);
            model.addAttribute("device", device);
        }

        // 🔹 2. select 유지용 값 세팅 (⭐ 중요)
        model.addAttribute("selectedRackId",
                model.containsAttribute("rackId")
                        ? model.getAttribute("rackId")
                        : (device.getRack() != null ? device.getRack().getId() : null)
        );

        model.addAttribute("selectedCateId",
                model.containsAttribute("cateId")
                        ? model.getAttribute("cateId")
                        : (device.getCategory() != null ? device.getCategory().getId() : null)
        );

        // 🔹 3. 공통 데이터
        model.addAttribute("racks", rackRepository.findAll());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("isEdit", true);

        return "device/device_form";
    }


    // ==========================================
    // 6. 실제 수정 처리
    // ==========================================
    @PostMapping("/devices/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam(value = "rackId", required = false) Long rackId,
                         @RequestParam(value = "cateId", required = false) String cateId,
                         Device device,
                         RedirectAttributes rttr) {
        try {
            // ===========================
            // 1. 검증 로직
            // ===========================
            if (device.getCompanyName() == null || device.getCompanyName().trim().isEmpty())
                throw new IllegalArgumentException("회사명은 필수 입력 항목입니다.");
            if (device.getCompanyPhone() == null || device.getCompanyPhone().trim().isEmpty())
                throw new IllegalArgumentException("회사 대표 번호는 필수 입력 항목입니다.");
            String phoneRegex = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
            if (!device.getCompanyPhone().matches(phoneRegex))
                throw new IllegalArgumentException("회사 대표 번호 형식이 올바르지 않습니다. (예: 02-123-4567)");
            if (device.getUserName() == null || device.getUserName().trim().isEmpty())
                throw new IllegalArgumentException("담당자 이름은 필수 입력 항목입니다.");
            if (device.getContact() == null || device.getContact().trim().isEmpty())
                throw new IllegalArgumentException("담당자 연락처는 필수 입력 항목입니다.");
            if (!device.getContact().matches(phoneRegex))
                throw new IllegalArgumentException("담당자 연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)");
            if (rackId == null) throw new IllegalArgumentException("설치할 랙(Rack)을 선택해야 합니다.");
            if (cateId == null || cateId.trim().isEmpty())
                throw new IllegalArgumentException("장비 종류(Category)를 선택해야 합니다.");
            if (device.getSerialNum() == null || device.getSerialNum().trim().isEmpty())
                throw new IllegalArgumentException("시리얼 번호는 필수 입력 항목입니다.");
            if (device.getStartUnit() == null || device.getStartUnit() < 1)
                throw new IllegalArgumentException("올바른 시작 유닛 번호를 입력해주세요.");
            if (device.getHeightUnit() == null || device.getHeightUnit() < 1)
                throw new IllegalArgumentException("장비 높이는 최소 1U 이상이어야 합니다.");

            if (deviceService.isSerialDuplicate(device.getSerialNum(), id))
                throw new IllegalStateException("이미 다른 장비에서 사용 중인 시리얼 번호입니다.");
            deviceService.checkRackOverlap(rackId, device.getStartUnit(), device.getHeightUnit(), id);

            // ===========================
            // 2. 저장
            // ===========================
            deviceService.updateDevice(id, device, rackId, cateId);
            rttr.addFlashAttribute("successMessage", "장비 정보가 수정되었습니다.");
            return "redirect:/devices";

        } catch (IllegalStateException | IllegalArgumentException e) {
            // ===========================
            // 3. 에러 메시지 및 입력값 유지
            // ===========================
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            rttr.addFlashAttribute("device", device);
            rttr.addFlashAttribute("isEdit", true);

            // ===========================
            // 4. select 유지용 값
            // ===========================
            if (rackId != null) rttr.addFlashAttribute("rackId", rackId);
            else if (device.getRack() != null) rttr.addFlashAttribute("rackId", device.getRack().getId());

            if (cateId != null) rttr.addFlashAttribute("cateId", cateId);
            else if (device.getCategory() != null) rttr.addFlashAttribute("cateId", device.getCategory().getId());

            // ===========================
            // 5. 다시 수정 화면으로 리다이렉트
            // ===========================
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

    }

    // 체크박스를 이용한 일괄 수정 및 삭제

    @PostMapping("/devices/batch-update")
    @ResponseBody
    public ResponseEntity<?> batchUpdate(@RequestBody deviceDTO dto) {
        // dto.getIds() 리스트를 순회하며 emsStatus나 status를 업데이트하는 로직 수행
        deviceService.updateMultipleDevices(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/devices/batch-delete")
    @ResponseBody
    public ResponseEntity<?> batchDelete(@RequestBody Map<String, List<Long>> payload) {
        List<Long> ids = payload.get("ids");
        deviceService.deleteMultipleDevices(ids);
        return ResponseEntity.ok().build();
    }

}