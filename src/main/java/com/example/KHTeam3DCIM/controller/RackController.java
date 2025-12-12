package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
import com.example.KHTeam3DCIM.dto.Rack.RackUpdateRequest;
import com.example.KHTeam3DCIM.service.DeviceService;
import com.example.KHTeam3DCIM.service.RackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RackController {

    private final RackService rackService;
    private final DeviceService deviceService;

    // ==========================================
    // 1. 랙 목록 페이지
    // ==========================================
    @GetMapping("/racks")
    public String list(Model model) {
        List<RackResponse> racks = rackService.findAllRacks();
        model.addAttribute("racks", racks);
        return "rack/rack_list";
    }

    // ==========================================
    // 2. 랙 등록 화면
    // ==========================================
    @GetMapping("/racks/new")
    public String createForm(Model model) {
        model.addAttribute("isEdit", false); // 등록 모드
        return "rack/rack_form";
    }

    // ==========================================
    // 3. 랙 실제 등록 처리
    // ==========================================
    @PostMapping("/racks/new")
    public String create(RackCreateRequest request) {
        rackService.addRack(request);
        return "redirect:/racks";
    }

    // ==========================================
    // 4. 랙 삭제 처리
    // ==========================================
    @GetMapping("/racks/{id}/delete")
    public String delete(@PathVariable Long id) {
        rackService.deleteRack(id);
        return "redirect:/racks";
    }

    // ==========================================
    // 5. 랙 실장도 화면
    // ==========================================
    @GetMapping("/racks/{id}/view")
    public String viewRack(@PathVariable Long id, Model model) {
        List<RackDetailDto> rackView = deviceService.getRackViewData(id);
        RackResponse rack = rackService.findRackById(id);
        model.addAttribute("rackView", rackView);
        model.addAttribute("rack", rack);
        return "rack/rack_view";
    }

    // ==========================================
    // 6. 랙 수정 화면 보여주기
    // ==========================================
    @GetMapping("/racks/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        // 기존 정보 가져오기
        RackResponse rack = rackService.findRackById(id);

        model.addAttribute("rack", rack);
        model.addAttribute("isEdit", true); // 수정 모드 ON

        return "rack/rack_form"; // 기존 폼 재활용
    }

    // ==========================================
    //  7. 랙 실제 수정 처리
    // ==========================================
    @PostMapping("/racks/{id}/edit")
    public String update(@PathVariable Long id, RackUpdateRequest request) {
        // Service에 있는 부분 수정 기능 활용
        rackService.updateRackPartially(id, request);
        return "redirect:/racks";
    }
}