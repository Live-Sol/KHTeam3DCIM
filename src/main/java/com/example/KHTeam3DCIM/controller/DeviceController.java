// 안내 데스크: DeviceController.java
// 사용자의 요청(URL)을 받아서, 만들어둔 DeviceService에게 일을 시키는 역할입니다.

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Category;
import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.domain.Rack;
import com.example.KHTeam3DCIM.domain.Request;
import com.example.KHTeam3DCIM.repository.RackRepository;
import com.example.KHTeam3DCIM.repository.RequestRepository;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    // ==========================================
    // 1. 장비 목록 페이지 보여주기 (+ 검색 기능)
    // ==========================================
    @GetMapping("/devices")
    public String list(Model model,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false, defaultValue = "latest") String sort,
                       @RequestParam(required = false, defaultValue = "desc") String sortDir) { // ⬅️ sortDir 추가!

        // 서비스에 sortDir까지 같이 전달
        List<Device> devices = deviceService.searchDevices(keyword, sort, sortDir);

        model.addAttribute("devices", devices);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("sortDir", sortDir); // ⬅️ 화면에서도 기억할 수 있게 모델에 담기

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

        // 실장도에서 빈칸 클릭하고 왔다면? -> 위치 자동 세팅
        if (rackId != null && startUnit != null) {
            device.setStartUnit(startUnit);
            // rackId는 아래 model.addAttribute("selectedRackId", ...)로 처리
        }

        // 신청서 승인 건 처리
        if (reqId != null) {
            Request req = requestRepository.findById(reqId).orElse(null);
            if (req != null) {
                device.setVendor(req.getVendor());
                device.setModelName(req.getModelName());
                device.setHeightUnit(req.getHeightUnit());
                model.addAttribute("selectedCateId", req.getCateId());
            }
        }

        // "대기 중인 신청서 목록" 가져오기 (드롭다운용)
        // WAITING 상태인 신청서들을 최신순으로 가져와서 모델에 담습니다.
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
    @Transactional // 상태 변경 때문에 트랜잭션 걸기
    public String create(
            @RequestParam("rackId") Long rackId,   // 폼에서 rackId 가져오기
            @RequestParam("cateId") String cateId, // 폼에서 cateId 가져오기
            @RequestParam(value = "reqId", required = false) Long reqId, // 폼에서 reqId 가져오기 (자동완성 여부 확인용)
            Device device // 나머지(모델명, 시리얼 등)는 알아서 객체에 담김
    ) {
        // 장비 등록 (저장해!)
        deviceService.registerDevice(rackId, cateId, device);

        // 만약 신청서 승인건이었다면, 신청서 상태를 '처리 완료'로 변경
        if (reqId != null) {
            Request req = requestRepository.findById(reqId).orElse(null);
            if (req != null) {
                req.setStatus("APPROVED"); // JPA기능 덕분에 자동으로 업데이트 처리됨
            }
        }
        return "redirect:/devices"; // 저장이 끝나면 목록 페이지로 강제 이동(Redirect)
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
        // 1. 수정할 장비 정보를 가져옴
        Device device = deviceService.findById(id);
        // (findById가 없다면 Service에 추가 필요, 혹은 Repo 직접 사용)
        // ※ Service에 findById가 없다면: deviceRepository.findById(id).get() 사용

        // 2. 드롭다운용 데이터 가져옴
        model.addAttribute("racks", rackRepository.findAll());
        model.addAttribute("categories", categoryService.findAllCategories());

        // 3. 화면에 전달
        model.addAttribute("device", device); // 기존 정보가 채워진 객체
        model.addAttribute("isEdit", true);   // "지금은 수정 모드야!" 라고 알려줌

        return "device/device_form"; // 등록 화면 재활용!
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
    // [추가] 7. 모달 팝업용 JSON 데이터 반환 API
    // ==========================================
    @GetMapping("/api/devices/{id}")
    @ResponseBody // HTML 파일이 아니라 데이터(JSON) 자체를 달라는 뜻
    public Device getDeviceDetailApi(@PathVariable Long id) {
        return deviceService.findById(id);
    }

    // ==========================================
    // [추가] 8. 전원 변경 API (AJAX용)
    // ==========================================
    @PostMapping("/api/devices/{id}/toggle-status")
    @ResponseBody
    public String toggleDeviceStatus(@PathVariable Long id) {
        return deviceService.toggleStatus(id);
    }

}