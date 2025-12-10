// /devices라고 요청이 오면, 서비스한테 목록 달라고 해서 HTML로 보내주는 것입니다.

package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.domain.Category;
import com.example.KHTeam3DCIM.domain.Device;
import com.example.KHTeam3DCIM.service.CategoryService;
import com.example.KHTeam3DCIM.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
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

    // 1. 장비 목록 페이지 보여주기
    @GetMapping("/devices")
    public String list(Model model) {
        // 서비스한테 "장비 다 가져와" 시키기
        List<Device> devices = deviceService.findAllDevices();

        // 가져온 보따리를 'devices'라는 이름표를 붙여서 HTML로 보냄
        model.addAttribute("devices", devices);

        return "device/list"; // templates/device/list.html을 찾아가라!
    }

    // 2. 장비 등록 화면 보여주기
    @GetMapping("/devices/new")
    public String createForm(Model model) {
        // 드롭다운 메뉴(서버, 스위치 등)를 채우기 위해 카테고리 목록도 같이 보냄
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);

        return "device/form"; // templates/device/form.html을 찾아가라!
    }

    // 3. 실제 등록 처리하기 (저장 버튼 눌렀을 때)
    @PostMapping("/devices/new")
    public String create(
            @RequestParam("rackId") Long rackId,   // 폼에서 rackId 가져오기
            @RequestParam("cateId") String cateId, // 폼에서 cateId 가져오기
            Device device // 나머지(모델명, 시리얼 등)는 알아서 객체에 담김
    ) {
        // 서비스 호출 (저장해!)
        deviceService.registerDevice(rackId, cateId, device);

        // 저장이 끝나면 목록 페이지로 강제 이동(Redirect)
        return "redirect:/devices";
    }
}