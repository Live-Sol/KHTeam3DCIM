package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
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
}