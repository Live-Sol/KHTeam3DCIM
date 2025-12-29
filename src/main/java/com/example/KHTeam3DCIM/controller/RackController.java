package com.example.KHTeam3DCIM.controller;

import com.example.KHTeam3DCIM.dto.Rack.RackCreateRequest;
import com.example.KHTeam3DCIM.dto.Rack.RackDetailDto;
import com.example.KHTeam3DCIM.dto.Rack.RackResponse;
import com.example.KHTeam3DCIM.dto.Rack.RackUpdateRequest;
import com.example.KHTeam3DCIM.service.DeviceService;
import com.example.KHTeam3DCIM.service.RackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RackController {

    private final RackService rackService;
    private final DeviceService deviceService;

    // 1. 랙 목록 페이지 (기존 유지)
    @GetMapping("/racks")
    public String list(Model model, @RequestParam(required = false) Long reqId, HttpServletRequest request) {
        List<RackResponse> racks = rackService.findAllRacks();
        model.addAttribute("request", request);
        model.addAttribute("racks", racks);
        model.addAttribute("reqId", reqId);
        return "rack/rack_list";
    }

    // 2. 랙 등록 화면 (수정: 빈 DTO 객체 추가)
    @GetMapping("/racks/new")
    public String createForm(Model model) {
        model.addAttribute("rackDto", new RackCreateRequest()); // Thymeleaf th:object 연결용
        model.addAttribute("isEdit", false);
        return "rack/rack_form";
    }

    // 3. 랙 실제 등록 처리 (수정: @Valid 및 에러 처리 추가)
    @PostMapping("/racks/new")
    public String create(@Valid @ModelAttribute("rackDto") RackCreateRequest rackDto,
                         BindingResult result, Model model,
                         RedirectAttributes rttr) {

        // [검증 1] DTO 내 어노테이션 검증 실패 시
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "rack/rack_form";
        }

        try {
            rackService.addRack(rackDto);
            // [추가] 성공 메시지 전달
            rttr.addFlashAttribute("msg", "신규 랙이 성공적으로 생성되었습니다.");
            rttr.addFlashAttribute("msgType", "success");

        } catch (IllegalArgumentException e) {
            // [검증 2] 서비스 레이어의 비즈니스 예외(이름 중복 등) 발생 시
            result.rejectValue("rackName", "duplicate", e.getMessage());
            model.addAttribute("isEdit", false);
            return "rack/rack_form";
        }

        return "redirect:/racks";
    }

    // 4. 랙 삭제 처리 (수정: 삭제 실패 시 메시지 전달 로직 추가)
    @GetMapping("/racks/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes rttr) {
        try {
            rackService.deleteRack(id);

            // [추가] 성공 메시지
            rttr.addFlashAttribute("msg", "랙이 삭제되었습니다.");
            rttr.addFlashAttribute("msgType", "success");

        } catch (IllegalStateException e) {
            // [변경] 실패 메시지를 SweetAlert용으로 전달
            rttr.addFlashAttribute("msg", "삭제 실패: " + e.getMessage());
            rttr.addFlashAttribute("msgType", "error");
        }
        return "redirect:/racks";
    }

    // 5. 랙 실장도 화면 (기존 유지)
    @GetMapping("/racks/{id}/view")
    public String viewRack(@PathVariable Long id, Model model, @RequestParam(required = false) Long reqId) {
        List<RackDetailDto> rackView = deviceService.getRackViewData(id);
        RackResponse rack = rackService.findRackById(id);
        model.addAttribute("rackView", rackView);
        model.addAttribute("rack", rack);
        model.addAttribute("reqId", reqId);
        return "rack/rack_view";
    }

    // 6. 랙 수정 화면 보여주기 (수정: DTO 매핑)
    @GetMapping("/racks/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        RackResponse rack = rackService.findRackById(id);

        // Response 객체 데이터를 수정용 DTO로 변환
        RackUpdateRequest updateDto = RackUpdateRequest.builder()
                .rackName(rack.getRackName())
                .totalUnit(rack.getTotalUnit())
                .locationDesc(rack.getLocationDesc())
                .build();

        model.addAttribute("rackDto", updateDto);
        model.addAttribute("rackId", id); // 폼 제출 시 ID 필요
        model.addAttribute("isEdit", true);
        return "rack/rack_form";
    }

    // 7. 랙 실제 수정 처리 (수정: @Valid 및 에러 처리 추가)
    @PostMapping("/racks/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("rackDto") RackUpdateRequest rackDto,
                         BindingResult result, Model model,
                         RedirectAttributes rttr) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("rackId", id);
            return "rack/rack_form";
        }

        try {
            rackService.updateRackPartially(id, rackDto);
            // [추가] 성공 메시지
            rttr.addFlashAttribute("msg", "랙 정보가 수정되었습니다.");
            rttr.addFlashAttribute("msgType", "success");
        } catch (IllegalArgumentException e) {
            // 이름 중복 혹은 높이 축소 불가 에러 처리
            result.rejectValue("rackName", "error", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("rackId", id);
            return "rack/rack_form";
        }

        return "redirect:/racks";
    }
}