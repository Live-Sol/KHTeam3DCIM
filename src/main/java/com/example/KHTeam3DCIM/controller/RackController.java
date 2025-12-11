package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
import com.example.KHTeam3DCIM.service.DeviceService;
import com.example.KHTeam3DCIM.service.RackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller // 이제 HTML 화면을 리턴합니다!
@RequiredArgsConstructor
public class RackController {

    private final RackService rackService;
    private final DeviceService deviceService;

    // 1. 랙 목록 페이지 (http://localhost:8080/racks)
    @GetMapping("/racks")
    public String list(Model model) {
        // 서비스에서 데이터 가져오기
        List<RackResponse> racks = rackService.findAllRacks();
        // 화면에 전달
        model.addAttribute("racks", racks);
        return "rack/rack_list"; // templates/rack/rack_list.html
    }

    // 2. 랙 등록 화면 보여주기
    @GetMapping("/racks/new")
    public String createForm() {
        return "rack/rack_form"; // templates/rack/rack_form.html
    }

    // 3. 랙 실제 등록 처리
    @PostMapping("/racks/new")
    public String create(RackCreateRequest request) {
        // 저장 로직 실행
        rackService.addRack(request);
        // 저장 후 목록 페이지로 이동
        return "redirect:/racks";
    }

    // 4. 랙 삭제 처리
    @GetMapping("/racks/{id}/delete") // 편의상 GET으로 처리 (원래는 DELETE 메소드가 정석)
    public String delete(@PathVariable Long id) {
        rackService.deleteRack(id);
        return "redirect:/racks";
    }

    // ==========================================
    // ⭐ [신규 추가] 랙 실장도 화면 연결
    // ==========================================
    @GetMapping("/racks/{id}/view")
    public String viewRack(@PathVariable Long id, Model model) {

        // 1. DeviceService에게 "42칸짜리 그림 데이터" 요청
        List<RackDetailDto> rackView = deviceService.getRackViewData(id);

        // 2. RackService에게 "이 랙 이름이랑 정보" 요청
        RackResponse rack = rackService.findRackById(id);

        // 3. 화면으로 보내기
        model.addAttribute("rackView", rackView);
        model.addAttribute("rack", rack);

        return "rack/rack_view"; // templates/rack/rack_view.html 파일로 이동
    }
}