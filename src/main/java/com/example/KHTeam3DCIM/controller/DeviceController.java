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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final CategoryService categoryService;
    private final RequestRepository requestRepostory;
    private final RackRepository rackRepository;


    // 1. 장비 목록 페이지 보여주기
    @GetMapping("/devices")
    public String list(Model model) {
        // 서비스한테 "장비 다 가져와" 시키기
        List<Device> devices = deviceService.findAllDevices();
        // 가져온 보따리를 'devices'라는 이름표를 붙여서 HTML로 보냄
        model.addAttribute("devices", devices);
        return "device/list"; // templates/device/list.html을 찾아가라!
    }

    // 2. 장비 등록 화면 보여주기 + 자동완성 기능 추가 (대수술)
    @GetMapping("/devices/new")
    public String createForm(Model model, @RequestParam(required = false) Long reqId) {

        // 빈 껍데기 장비 객체 생성
        Device device = new Device();

        // 만약 reqId가 있으면(=자동완성 요청이면) 서비스에 "이 reqId에 해당하는 랙 정보 좀 줘" 시키기
        if (reqId != null) {
            Request req = requestRepostory.findById(reqId).orElse(null);
            if (req != null) {
                // 신청서 내용을 장비 객체에 미리 채워넣기 (자동완성)
                device.setVendor(req.getVendor());
                device.setModelName(req.getModelName());
                device.setHeightUnit(req.getHeightUnit());
                // 카테고리는 객체가 필요해서, 뷰에서 처리하거나 여기서 처리
                // 편의상 뷰(HTML)에서 처리하도록 여기서는 생략하거나 단순 전달
                model.addAttribute("selectedCateId", req.getCateId());
            }
        }

        List<Rack> racks = rackRepository.findAll(); // 모든 랙 정보 가져오기
        model.addAttribute("racks", racks); // 랙 정보 넘기기
        model.addAttribute("categories", categoryService.findAllCategories()); // 모든 카테고리 정보 넘기기
        model.addAttribute("device", device); // 장비 객체 넘기기
        model.addAttribute("reqId", reqId); // reqId 넘기기 (자동완성 여부 확인용)

        return "device/form"; // templates/device/form.html을 찾아가라!
    }

    // 3. 실제 등록 처리하기 (저장 버튼 눌렀을 때)
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
            Request req = requestRepostory.findById(reqId).orElse(null);
            if (req != null) {
                req.setStatus("APPROVED"); // JPA기능 덕분에 자동으로 업데이트 처리됨
            }
        }
        return "redirect:/devices"; // 저장이 끝나면 목록 페이지로 강제 이동(Redirect)
    }
}